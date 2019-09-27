/*
   System Delay Sensor
 */


metadata {
    definition (name: "Latency Sensor", namespace: "asj", author: "asj") {
        capability "Switch"
        capability "PressureMeasurement"
        capability "Sensor"
    }

    preferences {
        input name: "autoOff", type: "bool", title: "Auto Off Timing", defaultValue: true
        input name: "autoOffTimeMs", type: "number", title: "Auto Off Time (ms)", defaultValue: 50, required: true
        input name: "tick", type: "bool", title: "Send Periodic Tick", defaultValue: false
        input name: "tickTimeMs", type: "number", title: "Time of Tick (ms)", defaultValue: 1000, required: true
        //standard logging options
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false
    }
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

//capability and device methods
def off() {
    if (device.currentValue("switch") == "off") return

    state.endTime = now()
    state.latency = state.endTime - state.startTime

    unschedule()

    if (logEnable) log.info "${device.displayName} On to off time took: ${state.latency}"
    def name = "pressure"
    def unit = "ms"
    def descriptionText = "${device.displayName} is ${state.latency} ms"
    sendEvent(name: "pressure",value: state.latency, descriptionText: txtEnable ? descriptionText : "",unit: unit)
    sendEvent(name: "switch", value: "off", isStateChange: true)
}

def on() {
    unschedule()
    sendEvent(name: "switch", value: "on", isStateChange: true)
    state.startTime = now()
    if (autoOff) runInMillis(autoOffTimeMs, off)
    if (tick) {
        def date = new Date()
        for (def offset = tickTimeMs; offset < autoOffTimeMs; offset += tickTimeMs) {
            date.setTime(state.startTime + offset)
            schedule(date, tick, [overwrite: false])
        }
    }
}

def tick() {
    if (device.currentValue("switch") == "off") return

    def latency = now() - state.startTime
    if (logEnable) log.info "${device.displayName} Tick at: ${latency}"
    def name = "pressure"
    def unit = "ms"
    def descriptionText = "${device.displayName} Tick at ${latency} ms"
    sendEvent(name: "pressure",value: latency, descriptionText: txtEnable ? descriptionText : "",unit: unit)
}

