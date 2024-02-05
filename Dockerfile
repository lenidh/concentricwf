FROM openjdk:21-bookworm

ENV SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" \
    ANDROID_HOME="/usr/local/android-sdk" \
    ANDROID_VERSION=34 \
    ANDORID_BUILD_TOOLS_VERSION=34.0.0

RUN mkdir -p "${ANDROID_HOME}" .android \
    && cd "${ANDROID_HOME}" \
    && curl -o sdk.zip ${SDK_URL} \
    && unzip sdk.zip \
    && rm sdk.zip \
    && mkdir "$ANDROID_HOME/licenses" || true \
    && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"
    #  yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses


RUN ${ANDROID_HOME}/cmdline-tools/bin/sdkmanager --sdk_root=${ANDROID_HOME} --update
RUN ${ANDROID_HOME}/cmdline-tools/bin/sdkmanager --sdk_root=${ANDROID_HOME} \
    "build-tools;${ANDORID_BUILD_TOOLS_VERSION}" \
    "platforms;android-${ANDROID_VERSION}" \
    "platform-tools"
