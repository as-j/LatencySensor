/*
   System Delay Sensor
 */


metadata {
    definition (name: "Latency Sensor", namespace: "asj", author: "asj") {
        capability "Switch"
        capability "TimedSession"
        attribute  "timeElapsed", "number"
    }

    preferences {
        input name: "autoStop", type: "bool", title: "Auto Stop", defaultValue: true
        input name: "autoStopTimeMs", type: "number", title: "Auto Stop Time (ms)", defaultValue: 50, required: true
        input name: "ignoreStart", type: "bool", title: "Ignore Start When Already Running", defaultValue: false
        //standard logging options
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false
    }
}

def logsStop(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

//capability and device methods
def stop() {
    if (device.currentValue("sessionStatus") != "running") return

    state.endTime = now()
    state.timeElapsed = state.endTime - state.startTime

    if (logEnable) log.info "${device.displayName} time elapsed ${state.timeElapsed}"
    sendEvent(name: "timeElapsed",
              value: state.timeElapsed,
              descriptionText: "${device.displayName} timeElapsed: ${state.timeElapsed} ms",
              unit: "ms")
    sendEvent(name: "sessionStatus", value: "stop")
}

def start() {
    if (ignoreStart && (device.currentValue("sessionStatus") == "running")) return
    sendEvent(name: "sessionStatus", value: "running")
    state.startTime = now()
    if (autoStop) runInMillis(autoStopTimeMs, stop)
}

def cancel() {
    sendEvent(name: "sessionStatus", value: "cancel")
}

def pause() {
    if (logEnable) log.info "${device.displayName} doesn't support pause"
}

def setTimeRemaining(timeRemaining) {
    if (timeRemaining) {
        device.updateSetting("autoStopTimeMs",[type:"number", value:timeRemaining])
        device.updateSetting("autoStop",[type:"bool", value: true])
        if (logEnable) log.info "${device.displayName} Enable and set autoStopTimeMs to ${autoStopTimeMs}"
    } else {
        device.updateSetting("autoStop",[type:"bool", value: false])
        if (logEnable) log.info "${device.displayName} Disabled autoStop"
    }
}

// Implement switch on/off so we can use the device in RM4
def on() {
    sendEvent(name: "switch", value: "on")
    start()
}

def off() {
    stop()
    sendEvent(name: "switch", value: "off")
}

