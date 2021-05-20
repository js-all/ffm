#!/bin/bash
api="https://authserver.mojang.com"
cuser=1
ctoken=1
! [ -e run/credentials ] && touch run/credentials
cwc=$(wc -l < run/credentials)
[ "$cwc" = "1" ] && ctoken=0
[ "$cwc" = "0" ] && cuser=0
[ "$cwc" = "0" ] && ctoken=0

if [ $cuser = 0 ]; then
    echo "username ?"
    read -r username
    echo "$username" > run/credentials
else
    username=$(head -n 1 < run/credentials)
fi

echo ""

if [ $ctoken = 1 ]; then
    echo "checking if current token is valid..."
    currentToken=$(head -n 3 < run/credentials | tail -n 1 | xargs)
    currentClient=$(head -n 2 < run/credentials | tail -n 1 | xargs)
    tokenvalid=$(curl -s -o /dev/null -w "%{http_code}" -d "{\"accessToken\": \"$currentToken\", \"clientToken\":\"$currentClient\"}" -X POST -H "Content-Type: application/json" $api/validate)
    if [ "$tokenvalid" = "403" ]; then
        echo "oops, invalid token"
        ctoken=0
        # remove everything but the first line (the username) in credentials, as the token cannot be trusted anymore
        head -n 1 < run/credentials > run/credentials.tmp && mv run/credentials.tmp run/credentials
    else
        echo "current token is valid, continuing !"
    fi
fi

if [ $ctoken = 0 ]; then
    echo "password ?"
    read -r -s password
    echo ""
    postdata=("{\"agent\": {\"name\": \"Minecraft\", \"version\": 1}, \"username\": \"$username\", \"password\": \"$password\"}")
    # get data with curl -> extract token and UUID with jq -> remove quotes with sed -> append to run/credentials
    curl -d "${postdata[@]}" -H "Content-Type: application/json" -X POST $api/authenticate | jq '.clientToken, .accessToken, .selectedProfile.id, .selectedProfile.name' | sed 's/"\(.\+\)"/\1/' >> run/credentials
fi