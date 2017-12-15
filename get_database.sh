adb -s emulator-5554 shell "run-as com.pgssoft.testwarez chmod 666 /data/data/com.pgssoft.testwarez/databases/TestWarez.db"
adb -s emulator-5554 pull /data/data/com.pgssoft.testwarez/databases/TestWarez.db
adb -s emulator-5554 shell "run-as com.pgssoft.testwarez chmod 600 /data/data/com.pgssoft.testwarez/databases/TestWarez.db"

#@echo off

#adb -s emulator-5554 shell "su -c 'cd /data/data/com.pgssoft.testwarez/database/;cp TestWarez.db /sdcard'"

#adb -s 192.168.56.101:5555 pull /sdcard/TestWarez.db

