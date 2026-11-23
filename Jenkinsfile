pipeline {
    agent none

    options {
        disableConcurrentBuilds()
    }

    environment {
        PLUGIN_NAME = 'com.cdsoftware.base'
        IDEMPIERE_VERSION = '12.0.0'

        JUMP_HOST = 'cloud.primware.net'
        JUMP_PORT = '21022'
        JUMP_FINGERPRINT = 'SHA256:kDjnmjhXTvHDHH3YzQwpzeWL5mFseqVtRuyNW+HbXuA'

        LXC_HOST = '192.168.0.102'
        LXC_PORT = '22'
        LXC_FINGERPRINT = 'SHA256:60ir3U8xOQ1cLmf1/McsBbLlc+cXMrHTkNsaD9+vFIs'

        OSGI_PORT = '12680'
        // Directorio aislado: no usar el current.jar de com.cdsoftware.theme.prim.
        REMOTE_RELEASES = '/opt/idempiere-plugin-deployer/releases/com.cdsoftware.base'
    }

    stages {
        stage('Validate') {
            agent {
                docker {
                    image 'carl0jgr/idempiere-source-builder:12'
                    args "--entrypoint='' -v /var/jenkins_home/.m2:/m2 -e MAVEN_OPTS=-Dmaven.repo.local=/m2"
                }
            }
            steps {
                sh './ci/validate-branch.sh "${BRANCH_NAME}" "${CHANGE_BRANCH:-}" "${CHANGE_TARGET:-}"'
                sh './ci/validate-plugin.sh'
            }
        }

        stage('Compile') {
            agent {
                docker {
                    image 'carl0jgr/idempiere-source-builder:12'
                    args "--entrypoint='' -v /var/jenkins_home/.m2:/m2 -e MAVEN_OPTS=-Dmaven.repo.local=/m2"
                }
            }
            steps {
                dir('target-platform') {
                    git branch: '12.0',
                        url: 'https://github.com/ingeint/idempiere-target-platform-plugin.git'
                    sh 'rm -f TargetPlatformPomGenerator*.class TargetPlatformPluginTagger*.class'
                    sh 'rm -rf target release'
                    sh './plugin-builder build ../${PLUGIN_NAME} ../${PLUGIN_NAME}.test'
                    sh '../ci/validate-artifacts.sh target "${BUILD_NUMBER}"'
                    sh '../ci/package-artifacts.sh target release "${BRANCH_NAME}" "${BUILD_NUMBER}"'
                    archiveArtifacts artifacts: 'release/*', fingerprint: true, allowEmptyArchive: false
                }

                // Como tambien se compila .test, seleccionar SOLO el JAR cuyo
                // Bundle-SymbolicName corresponde al plugin principal.
                sh '''#!/usr/bin/env bash
set -Eeuo pipefail
mkdir -p dist
shopt -s nullglob

candidates=(target-platform/target/*-"${IDEMPIERE_VERSION}.${BUILD_NUMBER}".jar)
[[ ${#candidates[@]} -gt 0 ]] || {
    echo 'ERROR: no hay JARs con la version esperada en target-platform/target' >&2
    ls -lah target-platform/target || true
    exit 1
}

TMP_MANIFEST=$(mktemp -d)
trap 'rm -rf "$TMP_MANIFEST"' EXIT
MATCHES=()
for source_jar in "${candidates[@]}"; do
    absolute_jar=$(realpath "$source_jar")
    rm -rf "$TMP_MANIFEST/META-INF"
    (cd "$TMP_MANIFEST" && jar xf "$absolute_jar" META-INF/MANIFEST.MF)
    manifest=$(tr -d '\\r' < "$TMP_MANIFEST/META-INF/MANIFEST.MF")
    symbolic=$(printf '%s\\n' "$manifest" | sed -n 's/^Bundle-SymbolicName: //p' | head -1)
    symbolic="${symbolic%%;*}"
    version=$(printf '%s\\n' "$manifest" | sed -n 's/^Bundle-Version: //p' | head -1)
    if [[ "$symbolic" == "$PLUGIN_NAME" ]]; then
        [[ "$version" == "${IDEMPIERE_VERSION}.${BUILD_NUMBER}" ]] || {
            echo "ERROR: version del manifiesto inesperada: $version" >&2
            exit 1
        }
        if grep -q '^Fragment-Host:' <<< "$manifest"; then
            echo 'ERROR: com.cdsoftware.base no debe compilarse como fragmento OSGi' >&2
            exit 1
        fi
        MATCHES+=("$source_jar")
    fi
done

[[ ${#MATCHES[@]} -eq 1 ]] || {
    echo "ERROR: se esperaba exactamente un JAR de $PLUGIN_NAME; encontrados ${#MATCHES[@]}" >&2
    printf 'Candidato: %s\\n' "${candidates[@]}"
    exit 1
}

DEST="dist/${PLUGIN_NAME}-${IDEMPIERE_VERSION}.${BUILD_NUMBER}.jar"
cp -- "${MATCHES[0]}" "$DEST"
printf '%s\\n' "${IDEMPIERE_VERSION}.${BUILD_NUMBER}" > dist/bundle-version.txt
printf 'JAR para despliegue: %s\\n' "$DEST"
'''
                archiveArtifacts artifacts: 'dist/*.jar', fingerprint: true, allowEmptyArchive: false
                stash name: 'idempiere-base-plugin', includes: 'dist/*.jar,dist/bundle-version.txt'
            }
            post {
                always {
                    script {
                        if (fileExists("${PLUGIN_NAME}.test/target/surefire-reports")) {
                            junit testResults: "${PLUGIN_NAME}.test/target/surefire-reports/*.xml", allowEmptyResults: false
                        } else {
                            echo 'Surefire reports were not produced because the build stopped before test execution.'
                        }
                    }
                }
                cleanup {
                    dir('target-platform') {
                        sh 'rm -rf target release ../${PLUGIN_NAME}/target ../${PLUGIN_NAME}.test/target'
                    }
                }
            }
        }

        stage('Deploy Demo') {
            when {
                beforeAgent true
                branch 'master'
            }
            agent any
            steps {
                dir('deploy-input-base') {
                    deleteDir()
                    unstash 'idempiere-base-plugin'
                    withCredentials([
                        sshUserPrivateKey(
                            credentialsId: 'jenkins-proxmox-jump',
                            keyFileVariable: 'JUMP_KEY',
                            usernameVariable: 'JUMP_USER'
                        ),
                        sshUserPrivateKey(
                            credentialsId: 'idempiere-demo-ssh',
                            keyFileVariable: 'LXC_KEY',
                            usernameVariable: 'LXC_USER'
                        )
                    ]) {
                        sh '''#!/usr/bin/env bash
set -Eeuo pipefail
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
KNOWN_HOSTS="$TMP/known_hosts"
LXC_HOST_KEY="$TMP/lxc_host_key"

ssh-keyscan -T 10 -p "$JUMP_PORT" -t ed25519 "$JUMP_HOST" > "$KNOWN_HOSTS"
FP=$(ssh-keygen -lf "$KNOWN_HOSTS" -E sha256 | awk '{print $2}')
[[ "$FP" == "$JUMP_FINGERPRINT" ]] || { echo 'ERROR: huella SSH del gateway incorrecta' >&2; exit 1; }

JUMP_OPTS=(-i "$JUMP_KEY" -p "$JUMP_PORT"
           -o IdentitiesOnly=yes -o BatchMode=yes
           -o StrictHostKeyChecking=yes -o "UserKnownHostsFile=$KNOWN_HOSTS")

ssh "${JUMP_OPTS[@]}" "$JUMP_USER@$JUMP_HOST" \\
    "ssh-keyscan -T 10 -p $LXC_PORT -t ed25519 $LXC_HOST 2>/dev/null" > "$LXC_HOST_KEY"
FP=$(ssh-keygen -lf "$LXC_HOST_KEY" -E sha256 | awk '{print $2}')
[[ "$FP" == "$LXC_FINGERPRINT" ]] || { echo 'ERROR: huella SSH de demo incorrecta' >&2; exit 1; }
cat "$LXC_HOST_KEY" >> "$KNOWN_HOSTS"

PROXY="ssh -i $JUMP_KEY -p $JUMP_PORT -o IdentitiesOnly=yes -o BatchMode=yes -o StrictHostKeyChecking=yes -o UserKnownHostsFile=$KNOWN_HOSTS -W %h:%p $JUMP_USER@$JUMP_HOST"
LXC_OPTS=(-i "$LXC_KEY"
          -o IdentitiesOnly=yes -o BatchMode=yes
          -o StrictHostKeyChecking=yes -o "UserKnownHostsFile=$KNOWN_HOSTS"
          -o "ProxyCommand=$PROXY")
LXC_DEST="$LXC_USER@$LXC_HOST"

JAR="dist/${PLUGIN_NAME}-${IDEMPIERE_VERSION}.${BUILD_NUMBER}.jar"
VERSION=$(cat dist/bundle-version.txt)
[[ "$VERSION" == "${IDEMPIERE_VERSION}.${BUILD_NUMBER}" ]] || {
    echo "ERROR: version esperada distinta de la compilada: $VERSION" >&2
    exit 1
}
SHA=$(sha256sum "$JAR" | awk '{print $1}')
REMOTE_JAR="$REMOTE_RELEASES/$(basename "$JAR")"

cat > "$TMP/deploy-base-osgi.sh" <<'REMOTE_SCRIPT'
#!/usr/bin/env bash
set -Eeuo pipefail
PLUGIN="$1"
EXPECTED_VERSION="$2"
NEW_JAR="$3"
EXPECTED_SHA="$4"
OSGI_PORT="$5"

RELEASES=$(dirname "$NEW_JAR")
CURRENT="$RELEASES/current.jar"

[[ -f "$NEW_JAR" ]] || { echo "ERROR: falta $NEW_JAR" >&2; exit 1; }
[[ $(sha256sum "$NEW_JAR" | awk '{print $1}') == "$EXPECTED_SHA" ]] || {
    echo 'ERROR: SHA-256 incorrecto en el servidor' >&2
    exit 1
}
command -v expect >/dev/null
command -v telnet >/dev/null

# Misma consola Gogo validada en el pipeline del tema Prim.
osgi() {
    OSGI_COMMAND="$1" OSGI_PORT="$OSGI_PORT" expect <<'EXPECT_SCRIPT' | tr -d '\\r'
set timeout 90
match_max 200000
spawn telnet 127.0.0.1 $env(OSGI_PORT)
expect {
    -re {g! ?$} {}
    timeout { puts stderr "ERROR: consola OSGi sin prompt"; exit 11 }
    eof { puts stderr "ERROR: consola OSGi cerrada"; exit 12 }
}
send -- "$env(OSGI_COMMAND)\\r"
expect {
    -re {g! ?$} {}
    timeout { puts stderr "ERROR: comando OSGi excedio timeout"; exit 13 }
    eof { puts stderr "ERROR: consola OSGi cerrada durante comando"; exit 14 }
}
send -- "disconnect\\r"
set timeout 10
expect {
    -re {Disconnect from console} {
        send -- "y\\r"
        exp_continue
    }
    eof {}
    timeout { puts stderr "ERROR: no se pudo cerrar sesion OSGi"; exit 15 }
}
EXPECT_SCRIPT
}

run_osgi() {
    local out
    out=$(osgi "$1") || { printf '%s\\n' "$out"; return 1; }
    printf '%s\\n' "$out"
    if grep -Eiq 'gogo:|BundleException|CommandNotFoundException|(^|[[:space:]])ERROR:' <<< "$out"; then
        echo "ERROR: consola reporto fallo en: $1" >&2
        return 1
    fi
}

SS=$(osgi ss)
mapfile -t IDS < <(printf '%s\\n' "$SS" | awk -v prefix="${PLUGIN}_" '
    $1 ~ /^[0-9]+$/ && index($3, prefix) == 1 { print $1 }
')
[[ ${#IDS[@]} -eq 1 ]] || {
    echo "ERROR: se esperaba exactamente un bundle $PLUGIN; encontrados ${#IDS[@]}" >&2
    printf '%s\\n' "$SS" | grep -F "$PLUGIN" || true
    exit 1
}
BUNDLE_ID="${IDS[0]}"
INSTALLED_VERSION=$(printf '%s\\n' "$SS" | awk -v id="$BUNDLE_ID" -v prefix="${PLUGIN}_" '
    $1 == id && index($3, prefix) == 1 {print substr($3, length(prefix) + 1)}
')
[[ -n "$INSTALLED_VERSION" ]] || { echo 'ERROR: version instalada desconocida' >&2; exit 1; }
echo "Bundle ID=$BUNDLE_ID; version actual=$INSTALLED_VERSION; nueva=$EXPECTED_VERSION"

healthy_version() {
    local expected="$1" listing="$2"
    printf '%s\\n' "$listing" | awk -v id="$BUNDLE_ID" -v expected="${PLUGIN}_${expected}" '
        $1 == id && $2 == "ACTIVE" && $3 == expected { found=1 }
        END { exit !found }
    '
}

verify_version() {
    local expected="$1" attempts="${2:-20}" listing i
    for ((i=1; i<=attempts; i++)); do
        if listing=$(osgi ss); then
            if healthy_version "$expected" "$listing"; then
                echo "OSGi OK: $PLUGIN $expected ACTIVE (ID $BUNDLE_ID)"
                return 0
            fi
            printf 'Verificacion OSGi %s/%s: ' "$i" "$attempts"
            printf '%s\\n' "$listing" | awk -v id="$BUNDLE_ID" '
                $1 == id {printf "[%s %s %s]", $1, $2, $3; found=1}
                END {if (!found) printf "[bundle no encontrado]"; print ""}
            '
        else
            echo "Consola OSGi no disponible (intento $i/$attempts)"
        fi
        if ((i < attempts)); then sleep 4; fi
    done
    return 1
}

promote_release() {
    local temp_link="$RELEASES/.current.$$"
    ln -s -- "$NEW_JAR" "$temp_link"
    mv -Tf -- "$temp_link" "$CURRENT"
}

# Solo considerar como rollback un JAR de la misma version instalada.
OLD_JAR=''
if [[ -L "$CURRENT" ]]; then
    candidate=$(readlink -f "$CURRENT" || true)
    if [[ -f "$candidate" && "$(basename "$candidate")" == "${PLUGIN}-${INSTALLED_VERSION}.jar" ]]; then
        OLD_JAR="$candidate"
    fi
fi
if [[ -z "$OLD_JAR" && -f "$RELEASES/${PLUGIN}-${INSTALLED_VERSION}.jar" ]]; then
    OLD_JAR="$RELEASES/${PLUGIN}-${INSTALLED_VERSION}.jar"
    echo "Rollback disponible: $OLD_JAR"
fi

if healthy_version "$EXPECTED_VERSION" "$SS"; then
    promote_release
    echo "DESPLIEGUE YA APLICADO: $PLUGIN $EXPECTED_VERSION (ID $BUNDLE_ID)"
    exit 0
fi

if [[ -z "$OLD_JAR" ]]; then
    echo 'ADVERTENCIA: primera actualizacion sin JAR anterior; rollback automatico NO disponible.'
fi

UPDATE_ATTEMPTED=0
on_exit() {
    local rc=$?
    trap - EXIT
    if (( rc != 0 && UPDATE_ATTEMPTED == 1 )); then
        if [[ -n "$OLD_JAR" && -f "$OLD_JAR" ]]; then
            echo "Intentando rollback a $INSTALLED_VERSION ($OLD_JAR)"
            if run_osgi "up $BUNDLE_ID file://$OLD_JAR" && \\
               verify_version "$INSTALLED_VERSION" 15; then
                echo 'ROLLBACK VERIFICADO'
            else
                echo 'ERROR: rollback no confirmado; revisar OSGi manualmente' >&2
            fi
        else
            echo 'ERROR: rollback automatico no disponible' >&2
        fi
    fi
    exit "$rc"
}
trap on_exit EXIT

UPDATE_ATTEMPTED=1
run_osgi "up $BUNDLE_ID file://$NEW_JAR"

# No ejecutar refresh: es un bundle ACTIVE, no un fragmento.
# OSGi debe volver a dejar el bundle ACTIVE tras su actualizacion.
if ! verify_version "$EXPECTED_VERSION" 20; then
    echo "ERROR: $PLUGIN $EXPECTED_VERSION no alcanzo ACTIVE" >&2
    exit 1
fi

promote_release
echo "DESPLIEGUE EXITOSO: $PLUGIN $EXPECTED_VERSION (ID $BUNDLE_ID)"
REMOTE_SCRIPT

ssh "${LXC_OPTS[@]}" -p "$LXC_PORT" "$LXC_DEST" \\
    "mkdir -p '$REMOTE_RELEASES' && command -v expect && command -v telnet"
scp "${LXC_OPTS[@]}" -P "$LXC_PORT" "$JAR" "$LXC_DEST:$REMOTE_JAR"
scp "${LXC_OPTS[@]}" -P "$LXC_PORT" "$TMP/deploy-base-osgi.sh" \\
    "$LXC_DEST:$REMOTE_RELEASES/deploy-base-osgi.sh"
ssh "${LXC_OPTS[@]}" -p "$LXC_PORT" "$LXC_DEST" \\
    "bash -n '$REMOTE_RELEASES/deploy-base-osgi.sh'"
ssh "${LXC_OPTS[@]}" -p "$LXC_PORT" "$LXC_DEST" \\
    "bash '$REMOTE_RELEASES/deploy-base-osgi.sh' '$PLUGIN_NAME' '$VERSION' '$REMOTE_JAR' '$SHA' '$OSGI_PORT'"
'''
                    }
                }
            }
        }
    }
}
