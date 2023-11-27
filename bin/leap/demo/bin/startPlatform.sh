#!/bin/sh

java -cp ../lib/jade.jar:../lib/chatOntology.jar:../lib/chatStandard.jar jade.Boot -gui -nomtp manager:chat.manager.ChatManagerAgent

