for /D %%g in (.\dotnet\examples\*) do copy /Y .\dotnet\VS2003S\AgentBooterJSharp.java %%g

for /D %%d in (%3\*) do %5 /recurse:%%d\*.java /reference:JadeLeap.dll /reference:VJSSupUILib.dll /t:exe /libpath:%2 /main:AgentBooterJSharp /out:%%d\test.exe
