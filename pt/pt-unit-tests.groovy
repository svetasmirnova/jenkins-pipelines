setup_rhel_tests = { ->
    sh '''
        sudo yum -y install epel-release
        sudo yum -y update
        sudo yum -y install tar
        sudo yum -y install jq
        sudo yum -y install libaio
        sudo yum -y install strace
        sudo yum -y install perl-Time-HiRes
        sudo yum -y install perl-Test-Harness
        sudo yum -y install perl-Test-Simple
        sudo yum -y install perl-Digest-MD5
        sudo yum -y install perl-File-Slurp
        sudo yum -y install perl-JSON
        sudo yum -y install perl-NetAddr-IP
        sudo yum -y install perl-Text-Diff
        sudo yum -y install perl-IPC-Cmd
        sudo yum -y install perl-IO-Socket-SSL
        sudo yum -y install perl-DBI
        sudo yum -y install perl-DBD-MySQL
    '''
}

setup_oel8_tests = { ->
    sh '''
        sudo yum -y update
        sudo yum -y install tar
        sudo yum -y install jq
        sudo yum -y install libaio
        sudo yum -y install strace
        sudo yum -y install perl-Time-HiRes
        sudo yum -y install perl-Test-Harness
        sudo yum -y install perl-Test-Simple
        sudo yum -y install perl-Digest-MD5
        sudo yum -y install perl-File-Slurp
        sudo yum -y install perl-JSON
        sudo yum -y install perl-NetAddr-IP
        sudo yum -y install perl-Text-Diff
        sudo yum -y install perl-IPC-Cmd
        sudo yum -y install perl-IO-Socket-SSL
        sudo yum -y install perl-DBI
        sudo yum -y install cpan
        sudo yum -y install gcc
        echo yes | sudo cpan install Test
        echo yes | sudo cpan upgrade JSON
        sudo dnf module -y disable mysql
        sudo yum install -y https://repo.percona.com/yum/percona-release-latest.noarch.rpm
        sudo percona-release setup -y pdps-8.0
        sudo yum -y install percona-server-server
        sudo yum -y install percona-server-client
        sudo yum -y install percona-server-devel
        echo yes | sudo cpan install DBD::mysql
    '''
    install_ssl()
}

setup_oel9_tests = { ->
    sh '''
        sudo yum -y install epel-release
        sudo yum -y update
        sudo yum -y install tar
        sudo yum -y install jq
        sudo yum -y install libaio
        sudo yum -y install strace
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
        sudo yum -y install perl-sigtrap
        sudo yum -y install perl-Text-Diff
        sudo yum -y install perl-IPC-Cmd
        sudo yum -y install perl-IO-Socket-SSL
        sudo yum -y install perl-Thread-Semaphore
        sudo yum -y install perl-DBI
        sudo yum -y install cpan
        sudo yum -y install gcc
        echo yes | sudo cpan upgrade JSON
        sudo yum install -y https://repo.percona.com/yum/percona-release-latest.noarch.rpm
        sudo percona-release setup -y pdps-8.0
        sudo yum -y install percona-server-server
        sudo yum -y install percona-server-client
        sudo yum -y install percona-server-devel
        echo yes | sudo cpan install DBD::mysql
    '''
    install_ssl()
}

setup_ubuntu_tests = { ->
    sh '''
        sudo sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/g' /etc/locale.gen
        sudo sudo locale-gen
        sudo apt-get update
        sudo apt-get install -y jq
        sudo apt-get install -y libnuma1
        sudo apt-get install -y strace
        sudo apt-get install -y libncurses6
        sudo apt-get install -y gawk
        sudo apt-get install -y lsof
        sudo apt-get install -y ncat
        sudo apt-get install -y libfile-slurp-perl
        sudo apt-get install -y libjson-perl
        sudo apt-get install -y libnetaddr-ip-perl
        sudo apt-get install -y libtext-diff-perl
        sudo apt-get install -y libio-socket-ssl-perl
        sudo apt-get install -y libipc-run-perl
        sudo apt-get install -y libdbi-perl
        sudo apt-get install -y libdbd-mysql-perl
        sudo apt-get install -y make
        sudo apt-get install -y gcc
    '''
}

install_ssl = { ->
    dir('sandbox') {
        sh '''
            curl -L https://github.com/openssl/openssl/releases/download/OpenSSL_1_1_1w/openssl-1.1.1w.tar.gz --output openssl-1.1.1w.tar.gz
            tar -xzf openssl-1.1.1w.tar.gz
            cd openssl-1.1.1w
            ./config --prefix=${SSL_PATH} shared
            make
            make install
        '''
    }
}

setup_jammy_tests ={ ->
    setup_ubuntu_tests()
    install_ssl()
}

setup_noble_tests = { ->
    setup_ubuntu_tests()
    install_ssl()
    sh '''
        sudo apt-get install -y libaio1t64
        sudo ln -s /usr/lib/x86_64-linux-gnu/libaio.so.1t64 /usr/lib/x86_64-linux-gnu/libaio.so.1
    '''
}

setup_debian_tests = { ->
    setup_ubuntu_tests()
    sh '''
        curl -O https://repo.percona.com/apt/percona-release_latest.generic_all.deb
        sudo apt-get -y install gnupg2 lsb-release
        sudo apt-get -y install ./percona-release_latest.generic_all.deb
        sudo apt-get update
        sudo percona-release setup pdps-8.0
        sudo apt-get -y upgrade perl
    '''
}

setup_bookworm_tests = { ->
    setup_debian_tests()
    install_ssl()
}

setup_bullseye_tests = { ->
    sh ''' 
        sudo sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/g' /etc/locale.gen
        sudo sudo locale-gen
        sudo apt-get update
        sudo DEBIAN_FRONTEND="noninteractive" apt-get install -y jq
        sudo DEBIAN_FRONTEND="noninteractive" apt-get install -y libnuma1
        sudo apt-get install -y strace
        sudo apt-get install -y libncurses6
        sudo apt-get install -y gawk
        sudo apt-get install -y lsof
        sudo apt-get install -y ncat
        sudo apt-get install -y libfile-slurp-perl
        sudo apt-get install -y libjson-perl
        sudo apt-get install -y libnetaddr-ip-perl
        sudo apt-get install -y libtext-diff-perl
        sudo apt-get install -y libio-socket-ssl-perl
        sudo apt-get install -y libipc-run-perl
        sudo apt-get install -y libdbi-perl
        sudo apt-get install -y libdbd-mysql-perl
        sudo apt-get install -y make
        sudo apt-get install -y gcc
    '''
    setup_debian_tests()
    install_ssl()
}

node_setups = [
    "min-ol-8-x64": setup_oel8_tests,
    "min-ol-9-x64": setup_oel9_tests,
    "min-focal-x64": setup_ubuntu_tests,
    "min-jammy-x64": setup_jammy_tests,
    "min-noble-x64": setup_noble_tests,
    "min-bullseye-x64": setup_bullseye_tests,
    "min-bookworm-x64": setup_bookworm_tests,
]

void setup_tests() {
    node_setups[params.node_to_test]()
}

pipeline {
    agent {
        label params.node_to_test
    }

    environment {
        LANG="en_US.UTF-8"
        LANGUAGE="en_US.UTF-8"
        LC_ALL="en_US.UTF-8"
        LC_CTYPE="en_US.UTF-8"
        PERCONA_TOOLKIT_BRANCH="${WORKSPACE}/percona-toolkit"
        TMP_DIR="/tmp"
        PERCONA_TOOLKIT_SANDBOX="${WORKSPACE}/sandbox/$MYSQL_BASEDIR"
        LOG_FILE="${WORKSPACE}/tmp/${TESTING_BRANCH}-${MYSQL_VERSION}.log"
        MYSQL_BASEDIR="Percona-Server-${MYSQL_MINOR}-Linux.x86_64.glibc${GLIBC}"
        DOWNLOAD_URL="https://downloads.percona.com/downloads/Percona-Server-${MYSQL_VERSION}/Percona-Server-${MYSQL_MINOR}/binary/tarball/"
        PATH="/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/ec2-user/.local/bin:${PERCONA_TOOLKIT_SANDBOX}/bin";
        SSL_PATH="${WORKSPACE}/sandbox/ssl"
        LD_LIBRARY_PATH="/usr/lib64:${SSL_PATH}/lib:${LD_LIBRARY_PATH}"
    }
    parameters {
        choice(
            choices: [
                'min-ol-8-x64',
                'min-ol-9-x64',
                'min-focal-x64',
                'min-jammy-x64',
                'min-noble-x64',
                'min-bullseye-x64',
                'min-bookworm-x64'
            ],
            description: 'Node to run tests on',
            name: 'node_to_test'
        )
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
                setup_tests() 
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
