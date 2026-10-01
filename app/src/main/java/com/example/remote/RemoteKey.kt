package com.example.remote

enum class RemoteKey(
    val label: String,
    val tizenKey: String,
    val rokuKey: String,
    val webosUri: String
) {
    POWER("Power", "KEY_POWER", "Power", "ssap://system/turnOff"),
    HOME("Home", "KEY_HOME", "Home", "ssap://system.launcher/open"),
    BACK("Back", "KEY_RETURN", "Back", "ssap://system/back"),
    MENU("Menu", "KEY_MENU", "Info", "ssap://ui/menu"),
    UP("Up", "KEY_UP", "Up", "ssap://ui/up"),
    DOWN("Down", "KEY_DOWN", "Down", "ssap://ui/down"),
    LEFT("Left", "KEY_LEFT", "Left", "ssap://ui/left"),
    RIGHT("Right", "KEY_RIGHT", "Right", "ssap://ui/right"),
    OK("OK", "KEY_ENTER", "Select", "ssap://ui/enter"),
    VOL_UP("Vol +", "KEY_VOLUP", "VolumeUp", "ssap://audio/volumeUp"),
    VOL_DOWN("Vol -", "KEY_VOLDOWN", "VolumeDown", "ssap://audio/volumeDown"),
    MUTE("Mute", "KEY_MUTE", "VolumeMute", "ssap://audio/setMute"),
    PLAY_PAUSE("Play/Pause", "KEY_PLAY", "Play", "ssap://media.controls/play"),
    CHANNEL_UP("CH +", "KEY_CHUP", "ChannelUp", "ssap://tv/channelUp"),
    CHANNEL_DOWN("CH -", "KEY_CHDOWN", "ChannelDown", "ssap://tv/channelDown"),
    INPUT_SOURCE("Input", "KEY_SOURCE", "InputTuner", "ssap://tv/switchInput")
}
