#!groovy

def devImage, err, mongoContainer
def repo = 'repo.jiksnu.org/'
def repoCreds = '8bb2c76c-133c-4c19-9df1-20745c31ac38'
def repoPath = 'https://repo.jiksnu.org/'

// Set build properties
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '5']],
            [$class: 'GithubProjectProperty', displayName: 'Jiksnu', projectUrlStr: 'https://github.com/duck1123/jiksnu/'],
            [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false]]);

stage 'Prepare Environment'

node {
    // Set current git commit
    checkout scm
    sh 'git submodule sync'
    sh "git rev-parse HEAD | tr -d '\n' | tee git-commit"
    env.GIT_COMMIT = readFile('git-commit').trim()

    sh 'git rev-parse --abbrev-ref HEAD | tee git-branch'
    env.GIT_BRANCH = readFile('git-branch').trim()

    sh 'git branch --contains HEAD -r | tee git-branches'
    def gbs = readFile('git-branches').trim()
    def gitBranches = gbs.tokenize('\n')

    def isPR = false

    for (branch in gitBranches) {
        if (branch.contains('origin/pr')) {
            isPR = true
            break
        }
    }

    if (env.BRANCH_NAME) {
        env.BRANCH_TAG = env.BRANCH_NAME.replaceAll('/', '-')
    } else if (isPR) {
        def matcher = gitBranches =~ /origin\/pr\/(\d+)\/\*/
        env.BRANCH_TAG = 'PR-' + matcher[0][1]
    } else {
        env.BRANCH_TAG = env.GIT_BRANCH.replaceAll('/', '-')
    }

    // Print Environment
    sh 'env | sort'
}

stage 'Build Dev Image'

node {
    devImage = docker.build("${repo}duck1123/jiksnu-dev", "docker/web-dev")

    docker.withRegistry('https://repo.jiksnu.org/', repoCreds) {
        devImage.push()
    }
}

stage 'Unit Tests'

node {
    try {
        mongoContainer = docker.image('mongo').run()

        devImage.inside("--link ${mongoContainer.id}:mongo -u root") {
            checkout scm

            wrap([$class: 'AnsiColorBuildWrapper']) {
                sh 'script/cibuild'
            }
        }

        step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/TEST-*.xml'])
    } catch (caughtError) {
        err = caughtError
    } finally {
        sh 'docker-compose stop'
        mongoContainer.stop()

        if (err) {
            throw err
        }
    }
}

stage 'Build Jars'

def clojureImage = docker.image('clojure')
clojureImage.pull()

clojureImage.inside('-u root') {
    checkout scm
    sh 'lein install'
    sh 'lein uberjar'
    archive 'target/*jar'
}

stage 'Build Run Image'

node {
    def mainImage = docker.build("${repo}duck1123/jiksnu:${env.BRANCH_TAG}")

    docker.withRegistry(repoPath, repoCreds) {
        mainImage.push()
    }
}

stage 'Generate Reports'

clojureImage.inside('-u root') {
    checkout scm
    sh 'lein doc'
    step([$class: 'JavadocArchiver', javadocDir: 'doc', keepAll: true])
    step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', pattern: '**/*.clj,**/*.cljs'])
}

// stage 'Integration tests'

// node {
//     try {
//         sh 'docker-compose up -d webdriver'
//         sh 'docker-compose up -d jiksnu-integration'

//         sh "docker inspect workspace_jiksnu-integration_1 | jq -r '.[].NetworkSettings.Networks.workspace_default.IPAddress' | tee jiksnu_host"
//         env.JIKSNU_HOST = readFile('jiksnu_host').trim()

//         sh "until \$(curl --output /dev/null --silent --fail http://${env.JIKSNU_HOST}/status); do echo '.'; sleep 5; done"
//         sh 'docker-compose run --rm web-dev script/protractor'
//     } catch (caughtError) {
//         err = caughtError
//     } finally {
//         sh 'docker-compose stop'
//         sh 'docker-compose rm -f'

//         if (err) {
//             throw err
//         }
//     }
// }
