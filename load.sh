#!/usr/bin/env bash

CONFIG=${HOME}/direct/config.properties
export CONFIG

if [ ! -f "$CONFIG" ]; then
    touch $CONFIG

    echo "devRestUrl=$DEV_REST_URL" >> ${CONFIG}
    echo "devWsUrl=$DEV_WS_URL" >> ${CONFIG}
    echo "uatRestUrl=$UAT_REST_URL" >> ${CONFIG}
    echo "uatWsUrl=$UAT_WS_URL" >> ${CONFIG}
    echo "devPlacementId=$DEV_PLACEMENT_ID" >> ${CONFIG}
    echo "devSecret=$DEV_SECRET" >> ${CONFIG}
    echo "uatPlacementId=$UAT_PLACEMENT_ID" >> ${CONFIG}
    echo "uatSecret=$UAT_SECRET" >> ${CONFIG}
    echo "prodPlacementId=$PROD_PLACEMENT_ID" >> ${CONFIG}
    echo "prodSecret=$PROD_SECRET" >> ${CONFIG}
    echo "testers=$TESTERS" >> ${CONFIG}

fi
