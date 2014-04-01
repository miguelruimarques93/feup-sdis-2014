@echo off
setlocal disableDelayedExpansion
(for /r %%F in (*.java) do (
  set "file=%%F"
  setlocal enableDelayedExpansion
  echo "!file:\=/!"
  endlocal
)) >sources.txt
mkdir bin
javac -d bin -cp "deps/*;" @sources.txt
del sources.txt