Set oShell = CreateObject("WScript.Shell")
oShell.CurrentDirectory = Left(WScript.ScriptFullName, InStrRev(WScript.ScriptFullName, "\") - 1)
oShell.Run "javaw -splash:Fondo.png -Dsun.awt.noerasebackground=true -jar AdminHotel.jar", 0, False