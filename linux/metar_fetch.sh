#!/bin/bash
CT=$(date -u +%H)
FILEF="http://weather.noaa.gov/pub/data/observations/metar/cycles/"$CT"Z.TXT"
FILEN="metars/"$CT"Z.TXT"
wget $FILEF -O $FILEN

