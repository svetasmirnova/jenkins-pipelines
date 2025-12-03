library changelog: false, identifier: "lib@main", retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/svetasmirnova/jenkins-pipelines.git'
])

def awsCredentials = [
    sshUserPrivateKey(
        credentialsId: 'MOLECULE_AWS_PRIVATE_KEY',
        keyFileVariable: 'MOLECULE_AWS_PRIVATE_KEY',
        passphraseVariable: '',
        usernameVariable: ''
    ),
    aws(
        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
        credentialsId: '7e252458-7ef8-4d0e-a4d5-5773edcbfa5e',
        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
    )
]

pipeline {
    agent {
        label 'min-bookworm-x64'
    }

    environment {
        MOLECULE_DIR = "pt/molecule"
        MYSQL_VERSION = "${params.MYSQL_VERSION}"
        MYSQL_MINOR = "${params.MYSQL_MINOR}"
        APP = "${params.APP}"
        PTDEBUG = "${params.PTDEBUG}"
        PTDEVDEBUG = "${params.PTDEVDEBUG}"
        TESTING_BRANCH = "${params.TESTING_BRANCH}"
        TEST_CMD = "${params.TEST_CMD}"

    }
    parameters {
        choice(
            choices: [
                'debian-11',
                'debian-12',
                'debian-13',
                'ol-8',
                'ol-9',
                'rh-10',
                'ubuntu-focal',
                'ubuntu-jammy',
                'ubuntu-noble',
            ],
            description: 'Node to run tests on',
            name: 'node_to_test'
        )
        choice(
            choices: [
                '8.4',
                '8.0',
                '5.7',
            ],
            description: 'Major version for Percona Server for MySQL',
            name: 'MYSQL_VERSION'
        )
        choice(
            choices: [
                '8.4.6-6',
                '8.4.5-5',
                '8.4.4-4',
                '8.4.3-3',
                '8.4.2-2',
                '8.4.0-1',
                '8.0.43-34',
                '8.0.42-33',
                '8.0.41-32',
                '8.0.40-31',
                '8.0.39-30',
                '8.0.37-29',
                '8.0.36-28',
                '8.0.35-27',
                '5.7.44-48',
                '5.7.43-47',
            ],
            description: 'Minor version for Percona Server for MySQL',
            name: 'MYSQL_MINOR'
        )
        choice(
            choices: [
                'mysql',
                'pxc',
            ],
            description: "Normal MySQL or PXC",
            name: 'APP'
        )
        choice(
            choices: [
                0,
                1,
            ],
            description: "Debug code (PTDEBUG)",
            name: 'PTDEBUG'
        )
        choice(
            choices: [
                0,
                1,
            ],
            description: "Debug test (PTDEVDEBUG)",
            name: 'PTDEVDEBUG'
        )
        string(
            defaultValue: '3.x',
            description: 'Branch for package-testing repository',
            name: 'TESTING_BRANCH'
        )
        string(
            defaultValue: 'prove -vr --trap --timer t',
            description: 'Test command',
            name: 'TEST_CMD'
        )
    }
    options {
        withCredentials(awsCredentials)
    }

    stages {
        stage('Set Build Name'){
            steps {
                script {
                    currentBuild.displayName = "${env.BUILD_NUMBER}-${node_to_test}"
                }
            }
        }

        stage('Checkout') {
            steps {
                deleteDir()
                git poll: false, branch: "main", url: "https://github.com/svetasmirnova/jenkins-pipelines.git"
            }
        }

        stage('Prepare') {
            steps {
                script {
                    installMolecule()
                }
            }
        }

        stage('RUN TESTS') {
                    steps {
                        script {

                                sh """
                                    echo PLAYBOOK_VAR="toolkit-testing" > .env.ENV_VARS
                                """

                            def envMap = loadEnvFile('.env.ENV_VARS')
                            withEnv(envMap) {
                                moleculeParallelTest(getNodeList(), env.MOLECULE_DIR)
                            }

                        }
                    }
        }
    }
}

def installMolecule() {
        sh """
            sudo apt update -y
            sudo apt install -y python3 python3-pip python3-dev python3-venv
            python3 -m venv virtenv
            . virtenv/bin/activate
            python3 --version
            python3 -m pip install --upgrade pip
            python3 -m pip install --upgrade setuptools
            python3 -m pip install --upgrade setuptools-rust
            python3 -m pip install --upgrade PyYaml==5.3.1 molecule==3.3.0 testinfra pytest molecule-ec2==0.3 molecule[ansible] "ansible<10.0.0" "ansible-lint>=5.1.1,<6.0.0" boto3 boto
        """
}

def loadEnvFile(envFilePath) {
    def envMap = []
    def envFileContent = readFile(file: envFilePath).trim().split('\n')
    envFileContent.each { line ->
        if (line && !line.startsWith('#')) {
            def parts = line.split('=')
            if (parts.length == 2) {
                envMap << "${parts[0].trim()}=${parts[1].trim()}"
            }
        }
    }
    return envMap
}

def getNodeList() {
    return [
        params.node_to_test
    ]
}
