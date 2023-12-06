setup_rhel_package_tests = { ->
    sh '''
        sudo yum -y install tar
        sudo yum -y install libaio
        sudo yum -y install perl-Time-HiRes
        sudo yum -y install perl-Test-Harness
        sudo yum -y install perl-Test-Simple
        sudo yum -y install perl-Digest-MD5
        sudo yum -y install perl-File-Slurp
        sudo yum -y install perl-JSON
        sudo yum -y install perl-NetAddr-IP
        sudo yum -y install perl-DBI
        sudo yum -y install perl-DBD-MySQL
    '''
}

setup_oel9_package_tests = { ->
    sh '''
        sudo yum -y install epel-release
        sudo yum -y update
        sudo yum -y install tar
        sudo yum -y install libaio
        sudo yum -y install perl-Time-HiRes
        sudo yum -y install perl-Test-Harness
        sudo yum -y install perl-Test-Simple
        sudo yum -y install perl-Digest-MD5
        sudo yum -y install perl-File-Slurp
        sudo yum -y install perl-JSON
        sudo yum -y install perl-NetAddr-IP
        sudo yum -y install perl-Locale-Codes
        sudo yum -y install perl-Thread
        sudo yum -y install perl-English
        sudo yum -y install perl-FindBin
        sudo yum -y install perl-Sys-Hostname
        sudo yum -y install perl-DBI
        sudo yum -y install perl-DBD-MySQL
    '''
}

setup_ubuntu_package_tests = { ->
    sh '''
        sudo apt-get update
        sudo apt-get install -y libnuma1
        sudo apt-get install -y libfile-slurp-perl
        sudo apt-get install -y libjson-perl
        sudo apt-get install -y libnetaddr-ip-perl
        sudo apt-get install -y libdbi-perl
        sudo apt-get install -y libdbd-mysql-perl
    '''
}

node_setups = [
    "min-centos-7-x64": setup_rhel_package_tests,
    "min-ol-8-x64": setup_rhel_package_tests,
    "min-ol-9-x64": setup_oel9_package_tests,
    "min-focal-x64": setup_ubuntu_package_tests,
    "min-jammy-x64": setup_ubuntu_package_tests,
    "min-buster-x64": setup_ubuntu_package_tests,
    "min-bullseye-x64": setup_ubuntu_package_tests,
]

void setup_package_tests() {
    node_setups[params.node_to_test]()
}

pipeline {
    agent {
        label params.node_to_test
    }

    environment {
        PATH = '/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/ec2-user/.local/bin';
        PERCONA_TOOLKIT_BRANCH = "${WORKSPACE}/percona-toolkit"
        TMP_DIR = "/tmp"
        PERCONA_TOOLKIT_SANDBOX = "${WORKSPACE}/sandbox/$MYSQL_BASEDIR"
        LOG_FILE = "${WORKSPACE}/tmp/${TESTING_BRANCH}-${MYSQL_VERSION}.log"
        MYSQL_BASEDIR="Percona-Server-${MYSQL_MINOR}-Linux.x86_64.glibc${GLIBC}"
        DOWNLOAD_URL="https://downloads.percona.com/downloads/Percona-Server-${MYSQL_VERSION}/Percona-Server-${MYSQL_MINOR}/binary/tarball/"
    }
    parameters {
        choice(
            choices: [
                'min-centos-7-x64',
                'min-ol-8-x64',
                'min-ol-9-x64',
                'min-focal-x64',
                'min-jammy-x64',
                'min-buster-x64',
                'min-bullseye-x64'
            ],
            description: 'Node to run tests on',
            name: 'node_to_test'
        )
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
        skipDefaultCheckout()
        disableConcurrentBuilds()
    }
    stages {
        stage('Set build name'){
            steps {
                script {
                    currentBuild.displayName = "#${env.BUILD_NUMBER}-${params.node_to_test}"
                }
            }
        }
        stage('Check version param and checkout') {
            steps {
                deleteDir()
                dir('percona-toolkit') {
                    git poll: false, branch: TESTING_BRANCH, url: "https://github.com/percona/percona-toolkit.git"
                }
            }
        }
        stage ('Prepare sandbox') {
            steps {
                setup_package_tests() 
                dir('sandbox') {
                    sh '''
                        curl ${DOWNLOAD_URL}/${MYSQL_BASEDIR}.tar.gz --output ${MYSQL_BASEDIR}.tar.gz
                        tar -xzf ${MYSQL_BASEDIR}.tar.gz
                    '''
                }
            }
        }
        stage ('Starting sandbox') {
            steps {
                dir('percona-toolkit') {
                        sh '''
                            util/check-dev-env
                            sandbox/test-env checkconfig
                            sandbox/test-env stop
                            sandbox/test-env kill
                            sandbox/test-env start
                        '''
                }
            }
        }
        stage ('Run tests') {
            steps {
                dir('percona-toolkit') {
                    sh '''
                        ${TEST_CMD}
                    '''
                }
            }
        }
        stage ('Clean up') {
            steps {
                dir('percona-toolkit') {
                    script {
                        sh '''
                            sandbox/test-env stop
                        '''
                    }
                }
            }
        }
    }
}
