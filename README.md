# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
## Chess Server Design
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYHSQ4AAaz5HRgyQyqRgotGMGACClHDCKAAHtCNIziSyTqDcSpyvyoIycSIVKbCkdLjAFJqUMBtfUZegAKK6lTYAiJW3HXKnbLmcoAFicAGZuv1RupgOTxlMfVBvGUVR07uq3R6wvJpeg+gd0BxMEbmeoHUU7ShymgfAgECG8adqyTVKUQFLMlbaR1GQztMba6djKUFBwOHKBdp2-bO2Oaz2++7MgofGBUrDgDvUiOq6vu2ypzO59vdzawcvToCLtmEdDkWoW1hH8C607s9dc2KYwwOUqxPAeu71BAJZoJMIH7CGlB1hGGDlAATE4TgJgMAGjLBUyPFM4GpJB0F4as5acKYXi+P4ATQOw5IwAAMhA0RJAEaQZFkyDmGyv7lNUdRNK0BjqAkaBYaq9ygXM+ivEsuxlghIKFF+zr-kMgGKU8LxvB85GYGpKn1lQ4IwAgrE8rCLFsai6KxNid6GCuRJruUREUoQcgoEOmmjLMulLKOrlnpOHIwNyvJWoKwo5n5hjnPAqQoCAsqBe8J4hSaYaOuajZxVAS7OQSp4suUG7edee5EceXbZYUU4urOLqHreeV1kZ5Q2TykSqB+hnnN+OUmep2HxdpfRESRpb-MpSE8ShMDoZhvRjbcWmfNMk2HtNMGzRWVHeH4gReCg6DMaxvjMBx6SZJgyHMGa1DOhU0g+kxPr1D6zQtKJqjid0U1Qegc0PoNzpA9BA1AohT2mRa5mXTu1lI2AdkYo57UuUy3blBwKDcFuO3A2g+7E9BwU4-V7LlNIBMUgl5Mg05HXg9mNlXb1-VGT+xTOs2rag+GC1gGhGFmJRnhHbRkJzkx0IwAA4oBrI3Vx90i3xfPZhUiufT99iAYDTNoKDqls+5JvQ0+WsNuUyCxMrSbWdCTsoOjDlFWydWkjA5JgFVZMQSTlPjqyYXlJFc5SUKYSQyDPvh3DZlSV72Nh-bruAb562jKHa7nuFUcFTAPIAGbQJsQZJLFqeJ7zdswP0htJmmFTN4BACS0hpgAjKhsZRk8nEDjh0kEX0OgIKA0pWnc2lTC3KAAHIbeRjQHCzw2dcxWejFzCCfmzDclFcfRL6obcd6M3d9wPQ9TCPKBz2vE9TzPL+4Zti+AavX-r5vbW80ciiyWhhLCF8r7ny7j3co-dB7D1us-MeC9J7T1Sp-ceXwl5-ywXsDepgDpSxooEbAPgoDYG4PAfshg3YpCQRrEBttT6VFqA0A2Rtggmwgb-V+Slfxsh3tfFefCATH2TgjCqmQ3awjgDQt2HssRpxKllX2-tA7xzQPnUKDUi48mjmPWO6orb12Gk5coqct4qKpr7KRKAZHCNvnA++iCuKYNQe-DBKDv7QNGLgxSextHU0apeP2gEjFLxLuXSu8RgxbzBjDbMcjNz2MAgfI+iST6jSXk4+BUZAHPRBMLEBYsVqONgXkiilZiHHQCJYAmFlkgwAAFIQB5ErcJARPHSkYbxOGL0qiUmEi0Jextg7QQTHMae9SoBwAgBZKAKwADqLBO5fRaAAISYgoOAABpbBMC76DwKaGc2iSrhTOADMuZCzlmrPWVsnZ+yng5NgTASp1shq5UbgAKzaWgGRrSeQKJQGiDGyiZClVJOow8QdiIh0yjY8OujI76IKnmWKmjEVhxPinMeELE5kgpDI15QSJwooimiyJ0SoBV3EhEmB2KC5mLymScJNK6VxKxtYjOKpsBaFHpQWEpKmU6JpkUZgry04JKfOUIFAK0nvkPp82G-TswCzbAI4aD1SkS2qdRWpXgrnwG4HgdU2BKFeWrvQ9WD1mEvTeh9L6P1jAquMuYmAIBTVwkxg2b2UKeyeu9bCMlyLxV00JoYG4sxNGzCXtKs5sqZD00yOkt1WT1Utk1UA7VItdUHSAA