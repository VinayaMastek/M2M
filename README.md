# M2M
This POC has been built to demonstrate how OM2M framework can be used to act as a bridge between various sensor devices like Heart Rate Monitor and Mobile or Web Applications from where the data emiting from the sensor devices can be used in specific context. 

Key Benefits of using OM2M framework
1. Multiple sensor devices can be integrated seamlessly with out the consuming applications having to worry about how to access this data
2. Consuming apps can subscribe to data relevant for their purpose at any given point in time

Android App
Since the Heart Rate monitor emits data using specific protocal, the android app acts as a gateway between the framework and the device..


IOTWebClient
This project provides the REST services to access OM2M processed data. In this demonstration the data from the heart rate monitor is regurlarly read and projected on the webclient using Google chart. To any fluctuations in pluse can be visually monitored

WebHtml2 
The html file used to project pluse data

