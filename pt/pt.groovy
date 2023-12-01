pipeline {
    environment {
        PATH = '/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/ec2-user/.local/bin';
        PERCONA_TOOLKIT_BRANCH = "${WORKSPACE}/percona-toolkit"
        TMP_DIR = "/tmp"
        PERCONA_TOOLKIT_SANDBOX = "${WORKSPACE}/sandbox/$MYSQL_BASEDIR"
        LOG_FILE = "${WORKSPACE}/tmp/${TESTING_BRANCH}-${MYSQL_VERSION}.log"
    }
    parameters {
        choice(
            choices: [
                'min-centos-7-x64',
                'min-ol-8-x64',
                'min-ol-9-x64',
                'min-bionic-x64',
                'min-focal-x64',
                'min-jammy-x64',
                'min-buster-x64',
                'min-bullseye-x64'
            ],
            description: 'Node to run tests on',
            name: 'node_to_test'
        )
        string(
            defaultValue: 'sveta-jenkins-test',
            description: 'Branch for package-testing repository',
            name: 'TESTING_BRANCH'
        )
        string(
            defaultValue: 'Percona-Server-8.0.34-26-Linux.x86_64.glibc2.17',
            description: 'MySQL Server directory',
            name: 'MYSQL_BASEDIR'
        )
        string(
            defaultValue: 'https://downloads.percona.com/downloads/Percona-Server-8.0/Percona-Server-8.0.34-26/binary/tarball/',
            description: 'Download URL, parent directory',
            name: 'DOWNLOAD_URL'
        )
        string(
            defaultValue: '8.0',
            description: 'Major version of MySQL server',
            name: 'MYSQL_VERSION'
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
        stage('Install'){
            steps {
                agent {
                    label 'min-centos-7-x64'
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
                dir('sandbox') {
                    script {
                        sh """
                            echo "Preparing sandbox"
                            sudo yum -y install perl-Test-Harness
                            sudo yum -y install libaio
                            sudo yum -y install perl-Test-Simple
                            sudo yum -y install perl-Digest-MD5
                            sudo yum -y install perl-DBI
                            sudo yum -y install perl-DBD-MySQL
                            curl ${DOWNLOAD_URL}/${MYSQL_BASEDIR}.tar.gz --output ${MYSQL_BASEDIR}.tar.gz
                            tar -xzf ${MYSQL_BASEDIR}.tar.gz
                        """
                    }
                }
                dir('percona-toolkit') {
                            //mkdir ${TMP_DIR}
                        sh """
                            sandbox/test-env restart
                        """
                }
            }
        }
        stage ('Running tests') {
            steps {
                dir('percona-toolkit') {
                    script {
                        sh 'prove -vr --trap --timer t/pt-heartbeat'
                    }
                }
            }
        }
    }
}
