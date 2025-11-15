void runNodeBuild(String node_to_test) {
    build(
        job: 'pt-unit-tests-molecule',
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
                        runNodeBuild('ol-8')
                    }
                }

                stage('OEL 9') {
                    steps {
                        runNodeBuild('ol-9')
                    }
                }

                stage('Ubuntu Focal') {
                    steps {
                        runNodeBuild('ubuntu-focal')
                    }
                }

                stage('Ubuntu Jammy') {
                    steps {
                        runNodeBuild('ubuntu-jammy')
                    }
                }

                stage('Ubuntu Noble Numbat') {
                    steps {
                        runNodeBuild('ubuntu-noble')
                    }
                }

                stage('Debian Bullseye') {
                    steps {
                        runNodeBuild('debian-11')
                    }
                }

                stage('Debian Bookworm') {
                    steps {
                        runNodeBuild('debian-12')
                    }
                }
            }
        }
    }
}

