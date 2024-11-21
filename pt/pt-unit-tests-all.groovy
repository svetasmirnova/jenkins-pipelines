void runNodeBuild(String node_to_test) {
    build(
        job: 'pt-unit-tests',
        parameters: [
            string(name: 'node_to_test', value: node_to_test),
            string(name: 'MYSQL_VERSION', value: params.MYSQL_VERSION),
            string(name: 'MYSQL_MINOR', value: params.MYSQL_MINOR),
            string(name: 'GLIBC', value: params.GLIBC),
            string(name: 'APP', value: params.APP),
            string(name: 'PTDEBUG', value: params.PTDEBUG),
            string(name: 'PTDEVDEBUG', value: params.PTDEVDEBUG),
            string(name: 'TESTING_BRANCH', value: params.TESTING_BRANCH),
            string(name: 'TEST_CMD', value: params.TEST_CMD),
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
                '8.4',
            ],
            description: 'Major version for Percona Server for MySQL',
            name: 'MYSQL_VERSION'
        )
        choice(
            choices: [
                '8.0.35-27',
                '8.0.36-28',
                '8.0.37-29',
                '8.0.39-30',
                '5.7.43-47',
                '5.7.44-48',
                '8.4.0-1',
                '8.4.2-2',
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
            defaultValue: '3.x',
            description: 'Branch for package-testing repository',
            name: 'TESTING_BRANCH'
        )
        string(
            defaultValue: 'prove -vr --trap --timer t',
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
                stage('OEL 8') {
                    steps {
                        runNodeBuild('min-ol-8-x64')
                    }
                }

                stage('OEL 9') {
                    steps {
                        runNodeBuild('min-ol-9-x64')
                    }
                }

                stage('Ubuntu Focal') {
                    steps {
                        runNodeBuild('min-focal-x64')
                    }
                }

                stage('Ubuntu Jammy') {
                    steps {
                        runNodeBuild('min-jammy-x64')
                    }
                }

                stage('Ubuntu Noble Numbat') {
                    steps {
                        runNodeBuild('min-noble-x64')
                    }
                }

                stage('Debian Bullseye') {
                    steps {
                        runNodeBuild('min-bullseye-x64')
                    }
                }

                stage('Debian Bookworm') {
                    steps {
                        runNodeBuild('min-bookworm-x64')
                    }
                }
            }
        }
    }
}

