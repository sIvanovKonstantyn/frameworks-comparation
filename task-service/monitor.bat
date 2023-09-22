@echo off
setlocal enabledelayedexpansion

:: Define the command to run
set "command_to_run=docker exec 7920e858fdb2 jcmd 1 VM.native_memory"

:: Define the output file
set "output_file=monitor.txt"

:loop
:: Run the command and capture the output
for /f "delims=" %%a in ('!command_to_run! 2^>^&1') do (
    set "output_line=%%a"
    echo !output_line! >> "!output_file!"

    :: Extract and transform the relevant values
    set "total_committed="
    set "java_heap_committed="
    set "gc_committed="

    :: Extract the committed values
    for /f "tokens=2" %%b in ("!output_line!") do (
        set "committed_value=%%b"
        set "committed_value=!committed_value:~0,-2!"

        if not defined total_committed (
            set "total_committed=!committed_value!"
        ) else if not defined java_heap_committed (
            set "java_heap_committed=!committed_value!"
        ) else if not defined gc_committed (
            set "gc_committed=!committed_value!"
        )
    )

    :: Check if all values are extracted and transform them into the desired format
    ::if defined total_committed (
    ::    if defined java_heap_committed (
    ::        if defined gc_committed (
                set "output=!total_committed!;!java_heap_committed!;!gc_committed!"

                :: Save the transformed output to the file
    ::            echo !output! >> "!output_file!"
    ::        )
    ::    )
    ::)
)

:: Sleep for 5 seconds (pause)
timeout /t 5 /nobreak >nul

:: Go back to the loop
goto :loop
