from shutil import copyfile
import string
import datetime

print ("Start preparing OTA html file..")

copyfile('../app/build/outputs/apk/app-unsignedDebugDev.apk', 'app-unsignedDebugDev.apk')
copyfile('../app/build/outputs/apk/app-unsignedDebugProd.apk', 'app-unsignedDebugProd.apk')

htmlTemplate = open('template.html').read()

gradleFile = open('../app/build.gradle')


with gradleFile as inputData :
	for line in inputData:
		if "versionCode" in line:
			versionCode = line.strip(' \n').split(" ")[1]
			pass

		if "versionName" in line:
			versionName = line.strip(' \n').split(" ")[1].strip('\"')
			pass


htmlTemplate = string.replace(htmlTemplate, '{VERSION}', versionName + "(" +  versionCode + ")")

date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
htmlTemplate = string.replace(htmlTemplate, '{DATE}', date)


indexHtml = open("index.html", "w")
indexHtml.write(htmlTemplate)
indexHtml.close()

print ("OTA Html file and and required fiels are ready to send.")

