pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'registry.hub.docker.com'
        ARTIFACTORY_URL = 'http://localhost:8082'|
        ARTIFACTORY_REPO = 'maven-java-hello'
        ARTIFACTORY_USERNAME = 'admin'
        ARTIFACTORY_PASSWORD = 'It-is-your-pass-word'
    }

    stages {
        stage('Compilar y Empaquetar') {
            steps {
                script {
                    // Compilar el programa Java
                    sh 'javac HolaMundo.java'

                    // Empaquetar en un archivo JAR (asumiendo que solo hay una clase en el archivo)
                    sh 'jar cvfe HolaMundo.jar HolaMundo HolaMundo.class'
                }
            }
        }

        stage('Construir Imagen Docker') {
            steps {
                script {
                    // Utiliza una imagen ligera con soporte para Java y Maven
                    sh 'docker build -t ${DOCKER_REGISTRY}/maven:3.8.7-eclipse-temurin-11 .'
                }
            }
        }

        stage('Subir Imagen Docker a Artifactory') {
            steps {
                script {
                    // Iniciar sesión en el registro Docker
                    sh 'docker login -u ${ARTIFACTORY_USERNAME} -p ${ARTIFACTORY_PASSWORD} ${DOCKER_REGISTRY}'

                    // Etiquetar la imagen con la URL de Artifactory
                    sh 'docker tag ${DOCKER_REGISTRY}/mi-app:latest ${ARTIFACTORY_URL}/${ARTIFACTORY_REPO}/mi-app:latest'

                    // Subir la imagen a Artifactory
                    sh 'docker push ${ARTIFACTORY_URL}/${ARTIFACTORY_REPO}/mi-app:latest'
                }
            }
        }

        stage('Registrar Artefacto en JFrog Artifactory') {
            steps {
                script {
                    // Utilizar el cliente JFrog CLI para registrar el artefacto en Artifactory
                    sh "jfrog rt u HolaMundo.jar ${ARTIFACTORY_URL}/${ARTIFACTORY_REPO}/Holas/HolaMundo.jar --user=${ARTIFACTORY_USERNAME} --password=${ARTIFACTORY_PASSWORD}"
                }
            }
        }
    }
}
