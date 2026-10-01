package com.example.discovery

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.example.data.local.DiscoveredDevice
import com.example.data.local.PairingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

data class NetworkDiagnosticInfo(
    val isWifiConnected: Boolean = false,
    val ssid: String = "",
    val localIp: String = "",
    val gatewayIp: String = "",
    val linkSpeedMbps: Int = 0,
    val isMulticastEnabled: Boolean = false
)

class DeviceDiscoveryEngine(private val context: Context) {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _networkInfo = MutableStateFlow(NetworkDiagnosticInfo())
    val networkInfo: StateFlow<NetworkDiagnosticInfo> = _networkInfo.asStateFlow()

    private var multicastLock: WifiManager.MulticastLock? = null

    init {
        refreshNetworkInfo()
    }

    fun refreshNetworkInfo() {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = connManager?.activeNetwork
            val caps = connManager?.getNetworkCapabilities(activeNet)

            val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val wifiInfo = wifiManager?.connectionInfo

            val rawSsid = wifiInfo?.ssid?.replace("\"", "") ?: ""
            val ssid = if (rawSsid.isNotEmpty() && rawSsid != "<unknown ssid>") rawSsid else if (isWifi) "Wi-Fi Network" else "Ethernet / Local Network"

            val ipInt = wifiInfo?.ipAddress ?: 0
            val ipStr = if (ipInt != 0) {
                "${ipInt and 0xff}.${ipInt shr 8 and 0xff}.${ipInt shr 16 and 0xff}.${ipInt shr 24 and 0xff}"
            } else {
                getDeviceIpAddress() ?: "127.0.0.1"
            }

            val linkSpeed = wifiInfo?.linkSpeed ?: 0

            _networkInfo.value = NetworkDiagnosticInfo(
                isWifiConnected = isWifi || caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true,
                ssid = ssid,
                localIp = ipStr,
                gatewayIp = getGatewayIp(ipStr),
                linkSpeedMbps = if (linkSpeed > 0) linkSpeed else 100,
                isMulticastEnabled = true
            )
        } catch (_: Exception) {
            _networkInfo.value = NetworkDiagnosticInfo()
        }
    }

    private fun getGatewayIp(localIp: String): String {
        val parts = localIp.split(".")
        return if (parts.size == 4) {
            "${parts[0]}.${parts[1]}.${parts[2]}.1"
        } else "192.168.1.1"
    }

    private fun getDeviceIpAddress(): String? {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    suspend fun scanForDevices(onDeviceFound: (DiscoveredDevice) -> Unit) = withContext(Dispatchers.IO) {
        if (_isScanning.value) return@withContext
        _isScanning.value = true
        _scanProgress.value = 0.05f

        acquireMulticastLock()

        try {
            refreshNetworkInfo()
            _scanProgress.value = 0.2f

            // 1. Real SSDP Multicast discovery for UPnP / DLNA / DIAL devices
            listenForSsdpDevices(onDeviceFound)
            _scanProgress.value = 0.6f

            // 2. Real subnet probe targeting actual TV control ports
            val localIp = _networkInfo.value.localIp
            if (localIp.isNotEmpty() && localIp != "127.0.0.1") {
                probeLocalSubnet(localIp, onDeviceFound)
            }
            _scanProgress.value = 1.0f
        } catch (_: Exception) {
            // Log or handle network exception gracefully
        } finally {
            releaseMulticastLock()
            _isScanning.value = false
        }
    }

    private fun acquireMulticastLock() {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifiManager?.createMulticastLock("omnicast_multicast_lock")?.apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (_: Exception) {}
    }

    private fun releaseMulticastLock() {
        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
    }

    private fun listenForSsdpDevices(onDeviceFound: (DiscoveredDevice) -> Unit) {
        val ssdpQuery = "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 2\r\n" +
                "ST: urn:schemas-upnp-org:device:MediaRenderer:1\r\n\r\n"

        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket().apply {
                soTimeout = 1200
            }
            val group = InetAddress.getByName("239.255.255.250")
            val sendData = ssdpQuery.toByteArray()
            val packet = DatagramPacket(sendData, sendData.size, group, 1900)
            socket.send(packet)

            val receiveBuffer = ByteArray(4096)
            val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)

            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 1500) {
                try {
                    socket.receive(receivePacket)
                    val response = String(receivePacket.data, 0, receivePacket.length)
                    val senderIp = receivePacket.address.hostAddress ?: continue

                    val device = parseSsdpResponse(response, senderIp)
                    if (device != null) {
                        onDeviceFound(device)
                    }
                } catch (_: java.net.SocketTimeoutException) {
                    break
                }
            }
        } catch (_: Exception) {
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private fun parseSsdpResponse(response: String, ip: String): DiscoveredDevice? {
        val lines = response.lines()
        var server = ""
        var location = ""
        var usn = ""

        for (line in lines) {
            val lower = line.lowercase()
            when {
                lower.startsWith("server:") -> server = line.substringAfter(":").trim()
                lower.startsWith("location:") -> location = line.substringAfter(":").trim()
                lower.startsWith("usn:") -> usn = line.substringAfter(":").trim()
            }
        }

        val brand = when {
            server.contains("samsung", ignoreCase = true) || response.contains("sec", ignoreCase = true) -> "Samsung"
            server.contains("webos", ignoreCase = true) || server.contains("lg", ignoreCase = true) -> "LG"
            server.contains("roku", ignoreCase = true) -> "Roku"
            server.contains("sony", ignoreCase = true) -> "Sony"
            server.contains("fire", ignoreCase = true) -> "Fire TV"
            else -> "Smart TV"
        }

        val name = when (brand) {
            "Samsung" -> "Samsung Smart TV"
            "LG" -> "LG webOS TV"
            "Roku" -> "Roku TV Player"
            "Sony" -> "Sony BRAVIA TV"
            "Fire TV" -> "Amazon Fire TV"
            else -> "Network Media Renderer"
        }

        return DiscoveredDevice(
            id = usn.ifEmpty { "ssdp_${ip.replace(".", "_")}" },
            name = name,
            brand = brand,
            ipAddress = ip,
            port = 1900,
            protocol = "SSDP / UPnP AVTransport",
            model = server.ifEmpty { "DLNA Media Renderer" },
            supports4K = true,
            avgLatencyMs = 60,
            pairingStatus = PairingStatus.UNPAIRED
        )
    }

    private fun probeLocalSubnet(localIp: String, onDeviceFound: (DiscoveredDevice) -> Unit) {
        val parts = localIp.split(".")
        if (parts.size != 4) return
        val subnetBase = "${parts[0]}.${parts[1]}.${parts[2]}"
        val hostNum = parts[3].toIntOrNull() ?: return

        // Scan nearby IP addresses in the subnet
        val hostsToProbe = mutableListOf<String>()
        // Check router gateway first
        hostsToProbe.add("$subnetBase.1")

        // Check a window around current device IP
        for (i in (hostNum - 10).coerceAtLeast(2)..(hostNum + 10).coerceAtMost(254)) {
            if (i != hostNum) {
                hostsToProbe.add("$subnetBase.$i")
            }
        }

        // Known TV control ports: 8060 (Roku), 8008 (Cast/DIAL), 8002 (Samsung), 3000 (LG)
        val portMap = listOf(
            8060 to ("Roku" to "Roku ECP"),
            8008 to ("Google Cast" to "Google Cast / DIAL"),
            8002 to ("Samsung" to "Tizen WebSocket"),
            3000 to ("LG" to "webOS SSAP")
        )

        for (host in hostsToProbe) {
            for ((port, brandInfo) in portMap) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(host, port), 80)
                        // If connection succeeds, a real device is alive on that port
                        val (brand, protocol) = brandInfo
                        val device = DiscoveredDevice(
                            id = "tcp_${host.replace(".", "_")}_$port",
                            name = "$brand ($host)",
                            brand = brand,
                            ipAddress = host,
                            port = port,
                            protocol = protocol,
                            model = "$brand Smart Screen",
                            supports4K = true,
                            avgLatencyMs = 65,
                            pairingStatus = if (brand == "Roku") PairingStatus.AUTHENTICATED else PairingStatus.UNPAIRED
                        )
                        onDeviceFound(device)
                    }
                } catch (_: Exception) {
                    // Port not open on host
                }
            }
        }
    }

    suspend fun pingDeviceLatency(ip: String, port: Int): Int = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 500)
            }
            (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
        } catch (_: Exception) {
            // Measure ICMP ping / reachability
            try {
                val addr = InetAddress.getByName(ip)
                if (addr.isReachable(500)) {
                    (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
                } else -1
            } catch (_: Exception) {
                -1
            }
        }
    }
}
