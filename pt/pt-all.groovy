void runNodeBuild(String node_to_test) {
    build(
        job: 'sveta-pt-tests',
        parameters: [
            string(name: 'node_to_test', value: node_to_test),
            string(name: 'MYSQL_VERSION', value: params.MYSQL_VERSION),
            string(name: 'MYSQL_MINOR', value: params.MYSQL_MINOR),
            string(name: 'GLIBC', value: params.GLIBC),
            string(name: 'APP', value: params.APP),
            booleanParam(name: 'PTDEBUG', value: params.PTDEBUG),
            booleanParam(name: 'PTDEVDEBUG', value: params.PTDEVDEBUG),
            booleanParam(name: 'TESTING_BRANCH', value: params.TESTING_BRANCH),
            booleanParam(name: 'TEST_CMD', value: params.TEST_CMD),
        ],
        propagate: true,
        wait: true
    )
}

pipeline {
    agent {
        label 'docker'
    }

    parameters {
        choice(
            choices: [
                '8.0',
                '5.7',
                //'8.1',
            ],
            description: 'Major version for Percona Server for MySQL',
            name: 'MYSQL_VERSION'
        )
        choice(
            choices: [
                '8.0.34-26',
                '5.7.43-47',
                //'8.1.0-1',
            ],
            description: 'Minor version for Percona Server for MySQL',
            name: 'MYSQL_MINOR'
        )
        choice(
            choices: [
                '2.17',
                '2.27',
                '2.28',
                '2.31',
                '2.34',
                '2.35',
            ],
            description: "GLIBC version",
            name: 'GLIBC'
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
            defaultValue: 'sveta-jenkins-test',
            description: 'Branch for package-testing repository',
            name: 'TESTING_BRANCH'
        )
        string(
            defaultValue: 'prove -vr --trap --timer t/pt-heartbeat',
            description: 'Test command',
            name: 'TEST_CMD'
        )
/*
        string(
            defaultValue: '',
            description: '',
            name: ''
        )
*/
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        skipDefaultCheckout()
    }

    stages {
        stage('Run parallel') {
            parallel {
                stage('Debian Buster') {
                    steps {
                        runNodeBuild('min-buster-x64')
                    }
                }

                stage('Debian Bullseye') {
                    steps {
                        runNodeBuild('min-bullseye-x64')
                    }
                }

                stage('Ubuntu Bionic') {
                    steps {
                        runNodeBuild('min-bionic-x64')
                    }
                }

                stage('Ubuntu Focal') {
                    steps {
                        runNodeBuild('min-focal-x64')
                    }
                }
            }
        }
    }
}

