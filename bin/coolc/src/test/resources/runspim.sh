#!/bin/bash
COOLRT=src/test/resources/coolrt.s

(/Users/abdul/Downloads/spim/spimsimulator-code/spim/a/spim -exception_file $COOLRT -file $1 | sed '1,/main/ d' | sed -n '2,$ p') > $2
