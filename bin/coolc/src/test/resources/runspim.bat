spim -exception_file src/test/resources/coolrt.s -file %1 | sed '1,/main/ d' | sed -n '2,$ p' > %2

