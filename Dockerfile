FROM java:8-alpine
MAINTAINER Juan Vazquez <juanvazquez@gmail.com>

ADD target/uberjar/web-enigma.jar /web-enigma/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/web-enigma/app.jar"]
