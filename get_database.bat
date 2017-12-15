@echo off

adb -s 192.168.56.101:5555 shell "su -c 'cd /data/data/com.pgs.evde.bcoins/testdata/;cp database_bcoins.db /sdcard/bcoins'"

adb -s 192.168.56.101:5555 pull /sdcard/bcoins/database_bcoins.db