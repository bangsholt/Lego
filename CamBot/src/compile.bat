nxjlink -gr -o .\Main.nxj -od .\Main.nxd Main
nxjc .\Main.java
nxjlink -gr -o .\Main.nxj -od .\Main.nxd Main
nxjupload -r .\Main.nxj
nxjconsole -di .\Main.nxd