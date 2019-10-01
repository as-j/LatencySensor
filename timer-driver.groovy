/*
   System Delay Sensor
 */


metadata {
    definition (name: "Timer Virtual Device", namespace: "asj", author: "asj") {
        capability "Switch"
        capability "TimedSession"
        attribute  "timeElapsed", "number"
    }

    preferences {
        input name: "autoOffS", type: "bool", title: "Auto Off Timing", defaultValue: true
        input name: "autoOffTimeS", type: "number", title: "Auto Off Time (s)", defaultValue: 120, required: true
        input name: "tick", type: "bool", title: "Send Periodic Tick", defaultValue: true
        input name: "tickTimeS", type: "number", title: "Time of Tick (s)", defaultValue: 60, required: true
        input name: "ignoreStart", type: "bool", title: "Ignore Start When Already Running", defaultValue: false
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
def stop() {
    if (device.currentValue("sessionStatus") == "stopped") return

    state.endTime = now()
    state.timeElapsed = (state.endTime - state.startTime)/1000
    state.timeRemaining = (state.targetEndingTime - now())/1000

    unschedule()

    if (logEnable) log.info "${device.displayName} Time Elapsed: ${state.timeElapsed} Time Remaining: ${state.timeRemaining}"
    def descriptionText = "${device.displayName} time remaining is 0 s"
    sendEvent(name:"timeRemaining", value: 0, descriptionText: descriptionText, unit: unit)
    descriptionText = "${device.displayName} time elapsed is ${state.timeElapsed} s"
    sendEvent(name:"timeElapsed", value: state.timeElapsed, descriptionText: descriptionText, unit: unit)
    sendEvent(name: "sessionStatus", value: "stopped")
}

def start() {
    if (ignoreStart && (device.currentValue("sessionStatus") == "running")) return
    unschedule()
    sendEvent(name: "sessionStatus", value: "running")
    state.startTime = now()
    state.targetEndingTime = state.startTime + 1000*autoOffTimeS
    def date = new Date()
    date.setTime(state.targetEndingTime)
    schedule(date, off)
    if (tick) {
        for (def offset = tickTimeS; offset < autoOffTimeS; offset += tickTimeS) {
            def tick_date = new Date()
            tick_date.setTime(state.startTime + 1000*offset)
            schedule(tick_date, tick, [overwrite: false])
        }
    }
}

def tick() {
    state.timeElapsed = (now() - state.startTime)/1000
    state.timeRemaining = (state.targetEndingTime - now())/1000
    if (state.timeRemaining < 0) state.timeRemaining = 0
    if (logEnable) log.info "${device.displayName} Time Elapsed: ${state.timeElapsed} Time Remaining: ${state.timeRemaining}"
    sendEvent(name: "timeRemaining", value:state.timeRemaining, descriptionText: "${device.displayName} Remaining time ${state.timeRemaining} ms",unit: unit)
    sendEvent(name:"timeElapsed", value:state.timeElapsed, descriptionText: "${device.displayName} Tick at ${state.timeElapsed} ms",unit: unit)
}

def setTimeRemaining(timeRemaining) {
    if (timeRemaining) {
        device.updateSetting("autoOffTimeS",[type:"number", value:timeRemaining])
        if (logEnable) log.info "${device.displayName} Set autoOffTime to ${autoOffTimeS}"
    } else {
        if (logEnable) log.info "${device.displayName} given an invalid timeRemaining value, ignorning"
    }
}

def on() {
    start()
}

def off() {
    stop()
}

def cancel() {
    unschedule()
    sendEvent(name: "sessionStatus", value: "cancel")
}
