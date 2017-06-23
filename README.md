# PrivPark Smart Blocker

## Description

PrivPark Smart Blocker is a prototype device of a smart parking blocker, which is part of PrivPark - the platform for renting private parking spots. This is our idea, which was created on HackXLR8 Hackathon during [London TechWeek 2017](https://snow.dog/blog/snow-dog-on-london-tech-week-2017/). 

## How it works?

This blocker should be mounted on a parking spot. When the owner doesn't want to lend their spot only the red led is turned on and blocker cannot be released. When the spot is available to park, only the yellow led is turned on. The car should be parked in front of the blocker, then a blocker takes a photo of license plate - it will turn on the white led. When the photo is taken the white led is turned off and the car's plate recognition starts which is indicated by blinking yellow led. It detects license plate and sends push to the driver's app. It contains information about available parking hours, price and cars license plate. When driver confirm the yellow led is turned off and the green turns on and the blocker is released. When car frees parking space, the spot is blocked and green led turns off and yellow turns on.

All process is shown on the graph below: 

![Alt Image Text](https://github.com/SnowdogApps/PrivPark-Smart-Blocker/blob/master/Diagram.jpg)

Simple demo movie of this behavior is available (version without plate and recognition): 
[here](https://www.youtube.com/watch?v=ukACkyV-1S8&feature=youtu.be)

## Scheme

![Alt Image Text](https://github.com/SnowdogApps/PrivPark-Smart-Blocker/blob/master/PrivParkSmartBlocker_scheme.jpg)

### Used pins:

* VCC: 5V (2)
* GND: Ground (6)
* White LED: BCM17 (11)
* Yellow LED: BCM27 (13)
* Green LED: BCM22 (15)
* Red LED: BCM23 (16)
* Ultrasonic Trigger: BCM5 (29)
* Ultrasonic Echo: BCM6 (31)


## Technologies

### Hardware

* Raspberry PI 3
* Ultrasonic Module HC-SR04 Distance Measuring Transducer Sensor 
* Raspberry Pi Camera v2.1
* 4 Resistors 220 Ohm
* 4 Led: Red, Green, Yellow, White 


### Software

* Android Things 0.4.1
* Firebase Database
* Google Cloud Vision API 
