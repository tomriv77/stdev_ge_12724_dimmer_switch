/**
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  GE 12724 Z-Wave Dimmer Switch
 *    (recycles code from Z-Wave Dimmer Switch)
 *
 *  Author: SmartThings, tomriv77
 *  Date: August 5, 2016
 */
metadata {
	definition (name: "GE 12724 Z-Wave Dimmer Switch", namespace: "tomriv77", author: "tomriv77") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        
        command "setMinLight"
        command "setHalfLight"
        command "setMaxLight"

		fingerprint type: "1101", mfr: "0063", prod: "4944", model: "3031"
        fingerprint cc: "26,27,73,70,86,72,77"
	}
    
    preferences {
    	input "dimLightLevel", "number",
        	title: "DIM level shortcut setting",
            defaultValue: 1,
            displayDuringSetup: true
        input "moderateLightLevel", "number",
        	title: "MODERATE level shortcut setting",
        	defaultValue: 50,
            displayDuringSetup: true
        input "brightLightLevel", "number",
        	title: "BRIGHT level shortcut setting",
        	defaultValue: 99,
            displayDuringSetup: true
    	input "invertSwitch", "boolean", 
			title: "Invert Switch",
			defaultValue: false,
			displayDuringSetup: true
    	input "ledIndicator", "enum",
			title: "LED Indicator",
			options: ["Lit When ON", "Lit When OFF", "Always OFF"],
            defaultValue: "Lit When OFF",
			required: false,
			displayDuringSetup: true
        input "dimRateLevelsManual", "number",
			title: "Manual Dim Rate Adjustment Levels",
            description: "Number of steps (or levels) that the dimmer will change while physical button is held. Enter a value 1-99.",
            range: "1..99",
			defaultValue: 1,
            required: false,
            displayDuringSetup: false
        input "dimRateTimingManual", "number",
			title: "Manual Dim Rate Adjustment Timing",
            description: "Adjust step timing (in 10 millisecond intervals). For example, a value of 3 means that the lighting level will change every 30 milliseconds when the Dim Command is received. Enter a value 1-255.",
            range: "1..255",
			defaultValue: 3,
            required: false,
            displayDuringSetup: false
        input "dimRateLevelsZwave", "number",
			title: "Z-Wave Dim Rate Adjustment Levels",
            description: "Number of steps (or levels) that the dimmer will change while button is held. Enter a value 1-99.",
            range: "1..99",
			defaultValue: 1,
            required: false,
            displayDuringSetup: false
        input "dimRateTimingZwave", "number",
			title: "Z-Wave Dim Rate Adjustment Timing",
            description: "Adjust step timing (in 10 millisecond intervals). For example, a value of 3 means that the lighting level will change every 30 milliseconds when the Dim Command is received. Enter a value 1-255.",
            range: "1..255",
			defaultValue: 3,
            required: false,
            displayDuringSetup: false
    }

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("maxLight", "device.switchLevel", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "default", action: "setMaxLight", label: "BRIGHT", icon: "st.illuminance.illuminance.bright"
        }
        
        standardTile("halfLight", "device.switchLevel", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "default", action: "setHalfLight", label: "MODERATE", icon: "st.illuminance.illuminance.light"
        }
        
        standardTile("minLight", "device.switchLevel", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "default", action: "setMinLight", label: "DIM", icon: "st.illuminance.illuminance.dark"
        }

		main(["switch"])
		details(["switch", "minLight", "halfLight", "maxLight", "refresh"])
	}
}

def configure() {
	log.debug "configure() was called"
    
	def __invertSwitch = 0
    log.debug "invertSwitch: ${invertSwitch}"
    if(invertSwitch != null && true == invertSwitch) {
    	__invertSwitch = 1
    }
    
    def __ledIndicator = 0
    if(ledIndicator == "Lit When OFF") {
    	__ledIndicator = 0
    } else if(ledIndicator == "Lit When ON") {
    	__ledIndicator = 1
    } else {
    	__ledIndicator = 2
    }
    
    def __dimRateLevelsManual = 1
    if(dimRateLevelsManual) {
    	__dimRateLevelsManual = dimRateLevelsManual
    }
    
    def __dimRateTimingManual = 3
    if(dimRateTimingManual) {
    	__dimRateTimingManual = dimRateTimingManual
    }
    
    def __dimRateLevelsZwave = 1
    if(dimRateLevelsZwave) {
    	__dimRateLevelsZwave = dimRateLevelsZwave
    }
    
    def __dimRateTimingZwave = 3
    if(dimRateTimingZwave) {
    	__dimRateTimingZwave = dimRateTimingZwave
    }
	
	def cmd = delayBetween([        
        // configure switch orientation
        zwave.configurationV1.configurationSet(parameterNumber: 0x04, configurationValue: [__invertSwitch], size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 0x04).format(),
        
        // configure LED indicator
        zwave.configurationV1.configurationSet(parameterNumber: 0x03, configurationValue: [__ledIndicator], size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 0x03).format(),
        
        // configure manual dim rate settings
        zwave.configurationV1.configurationSet(parameterNumber: 0x09, configurationValue: [__dimRateLevelsManual], size: 1).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 0x09).format(),
        zwave.configurationV1.configurationSet(parameterNumber: 0x0a, configurationValue: [__dimRateTimingManual], size: 1).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 0x0a).format(),
        
        // configure z-wave dim rate settings
        zwave.configurationV1.configurationSet(parameterNumber: 0x07, configurationValue: [__dimRateLevelsZwave], size: 1).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 0x07).format(),
        zwave.configurationV1.configurationSet(parameterNumber: 0x08, configurationValue: [__dimRateTimingZwave], size: 1).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 0x08).format()
    ], 3000)
    
    //log.debug cmd
  	cmd
}

def updated() {
	log.debug "updated() was called"
    response(configure())
}

def setMaxLight(){
    int nextLevel = brightLightLevel
    log.debug "Setting dimmer level up to: ${nextLevel}"
    setLevel(nextLevel)
}

def setHalfLight() {
    int nextLevel = moderateLightLevel
    log.debug "Setting dimmer level to: ${nextLevel}"
    setLevel(nextLevel)
}

def setMinLight() {
    int nextLevel = dimLightLevel
    log.debug "Setting dimmer level to: ${nextLevel}"
    setLevel(nextLevel)
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    // parameterNumber: 3		Indicator Light		[0 - when off, 1 - when on, 2 - never]
    // parameterNumber: 4		Invert Switch		[0, 1]
    // parameterNumber: 7		Z-Wave Dim Levels	[1 - 99]
    // parameterNumber: 8		Z-Wave Dim Timing	[1 - 255]
    // parameterNumber: 9		Manual Dim Levels	[1 - 99]
    // parameterNumber: 10		Manual Dim Timing	[1 - 255]

    switch (cmd.parameterNumber) {
    	case 3:
        	def ilv = "when off"
			if (cmd.configurationValue[0] == 1) {ilv = "when on"}
			if (cmd.configurationValue[0] == 2) {ilv = "never"}
            log.debug "${device.displayName} (Indicator Light) parameter[${cmd.parameterNumber}] size[${cmd.size}] value${cmd.configurationValue} ${ilv}"
        	break
        case 4:
        	def isv = "NORMAL"
            if(cmd.configurationValue[0] == 1) {isv = "INVERTED"}
        	log.debug "${device.displayName} (Switch Invert State) parameter[${cmd.parameterNumber}] size[${cmd.size}] value${cmd.configurationValue} ${isv}"
        	break
        case 7:
        	log.debug "${device.displayName} (Z-Wave Dim Levels) parameter[${cmd.parameterNumber}] size[${cmd.size}] value${cmd.configurationValue}"
        	break
        case 8:
        	log.debug "${device.displayName} (Z-Wave Dim Timing) parameter[${cmd.parameterNumber}] size[${cmd.size}] value${cmd.configurationValue}"
        	break
        case 9:
        	log.debug "${device.displayName} (Manual Dim Levels) parameter[${cmd.parameterNumber}] size[${cmd.size}] value${cmd.configurationValue}"
        	break
        case 10:
        	log.debug "${device.displayName} (Manual Dim Timing) parameter[${cmd.parameterNumber}] size[${cmd.size}] value${cmd.configurationValue}"
        	break
        default:
        	log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
    }
	
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    [:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands,100)
}