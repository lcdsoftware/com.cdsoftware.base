@echo off

set DEBUG_MODE=

if "%1" == "debug" (
  set DEBUG_MODE=debug
)

cd com.cdsoftware.lirionizer.targetplatform
call .\plugin-builder.bat %DEBUG_MODE% ..\com.cdsoftware.lirionizer ..\com.cdsoftware.lirionizer.test
cd ..
