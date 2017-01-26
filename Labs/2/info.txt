MyServer.java est le programme Java du serveur program et MyClientWin.java celui du client.
 
les programmes Java peuvent être compilés (sur un ordinateur ayant le JDK ou JRE installé) 
à l'aide des commandes suivantes:
 
javac ChatClient.java
javac ChatClient.java
javac ClientSocketManager.java
javac ServerSocketManager.java
 
Le programme du serveur peut être exécuté avec un paramètre – numéro de port:
 
java ChatServer 3333
(toute autre numéro de port valide peut être utilisé). Par défaut, le numéro de port est 4444.  Assurez-vous que le fichier
ServerSocketManater.class soit présent dans le mêm répertoire.
 
Le programme du client peut être exécute sur toute autre station (ayant JDK ou JRE) 
à l’aide de la commande suivante:
 
java ChatClient
 
Lorsque le bouton "connecte" est pressé, le programme demandera l’adresse IP du serveur, 
le nom de la personne qui rejoint le forum/chat, (utilisé pour to identifier les participants), 
et le numéro de port (4444 dans si le port par défaut est utilisé dans le serveur). Assurez-vous que le fichier
ClientSocketManater.class soit présent dans le mêm répertoire.

 
