/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.security;

import android.content.Context;

import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore.ProtectionParameter;
import java.util.Date;

/**
 * This provides the optional parameters that can be specified for
 * {@code KeyStore} entries that work with
 * <a href="{@docRoot}training/articles/keystore.html">Android KeyStore
 * facility</a>. The Android KeyStore facility is accessed through a
 * {@link java.security.KeyStore} API using the {@code AndroidKeyStore}
 * provider. The {@code context} passed in may be used to pop up some UI to ask
 * the user to unlock or initialize the Android KeyStore facility.
 * <p>
 * Any entries placed in the {@code KeyStore} may be retrieved later. Note that
 * there is only one logical instance of the {@code KeyStore} per application
 * UID so apps using the {@code sharedUid} facility will also share a
 * {@code KeyStore}.
 * <p>
 * Keys may be generated using the {@link KeyPairGenerator} facility with a
 * {@link KeyPairGeneratorSpec} to specify the entry's {@code alias}. A
 * self-signed X.509 certificate will be attached to generated entries, but that
 * may be replaced at a later time by a certificate signed by a real Certificate
 * Authority.
 */
public final class KeyStoreParameter implements ProtectionParameter {
    private int mFlags;
    private final Date mKeyValidityStart;
    private final Date mKeyValidityForOriginationEnd;
    private final Date mKeyValidityForConsumptionEnd;
    private final @KeyStoreKeyConstraints.PurposeEnum int mPurposes;
    private final @KeyStoreKeyConstraints.PaddingEnum int mPaddings;
    private final @KeyStoreKeyConstraints.DigestEnum Integer mDigests;
    private final @KeyStoreKeyConstraints.BlockModeEnum int mBlockModes;
    private final @KeyStoreKeyConstraints.UserAuthenticatorEnum int mUserAuthenticators;
    private final int mUserAuthenticationValidityDurationSeconds;

    private KeyStoreParameter(int flags,
            Date keyValidityStart,
            Date keyValidityForOriginationEnd,
            Date keyValidityForConsumptionEnd,
            @KeyStoreKeyConstraints.PurposeEnum int purposes,
            @KeyStoreKeyConstraints.PaddingEnum int paddings,
            @KeyStoreKeyConstraints.DigestEnum Integer digests,
            @KeyStoreKeyConstraints.BlockModeEnum int blockModes,
            @KeyStoreKeyConstraints.UserAuthenticatorEnum int userAuthenticators,
            int userAuthenticationValidityDurationSeconds) {
        if ((userAuthenticationValidityDurationSeconds < 0)
                && (userAuthenticationValidityDurationSeconds != -1)) {
            throw new IllegalArgumentException(
                    "userAuthenticationValidityDurationSeconds must not be negative");
        }

        mFlags = flags;
        mKeyValidityStart = keyValidityStart;
        mKeyValidityForOriginationEnd = keyValidityForOriginationEnd;
        mKeyValidityForConsumptionEnd = keyValidityForConsumptionEnd;
        mPurposes = purposes;
        mPaddings = paddings;
        mDigests = digests;
        mBlockModes = blockModes;
        mUserAuthenticators = userAuthenticators;
        mUserAuthenticationValidityDurationSeconds = userAuthenticationValidityDurationSeconds;
    }

    /**
     * @hide
     */
    public int getFlags() {
        return mFlags;
    }

    /**
     * Returns {@code true} if this parameter requires entries to be encrypted
     * on the disk.
     */
    public boolean isEncryptionRequired() {
        return (mFlags & KeyStore.FLAG_ENCRYPTED) != 0;
    }

    /**
     * Gets the time instant before which the key is not yet valid.
     *
     * @return instant or {@code null} if not restricted.
     * @hide
     */
    public Date getKeyValidityStart() {
        return mKeyValidityStart;
    }

    /**
     * Gets the time instant after which the key is no long valid for decryption and verification.
     *
     * @return instant or {@code null} if not restricted.
     *
     * @hide
     */
    public Date getKeyValidityForConsumptionEnd() {
        return mKeyValidityForConsumptionEnd;
    }

    /**
     * Gets the time instant after which the key is no long valid for encryption and signing.
     *
     * @return instant or {@code null} if not restricted.
     *
     * @hide
     */
    public Date getKeyValidityForOriginationEnd() {
        return mKeyValidityForOriginationEnd;
    }

    /**
     * Gets the set of purposes for which the key can be used.
     *
     * @hide
     */
    public @KeyStoreKeyConstraints.PurposeEnum int getPurposes() {
        return mPurposes;
    }

    /**
     * Gets the set of padding schemes to which the key is restricted.
     *
     * @hide
     */
    public @KeyStoreKeyConstraints.PaddingEnum int getPaddings() {
        return mPaddings;
    }

    /**
     * Gets the set of digests to which the key is restricted.
     *
     * @throws IllegalStateException if this restriction has not been specified.
     *
     * @see #isDigestsSpecified()
     *
     * @hide
     */
    public @KeyStoreKeyConstraints.DigestEnum int getDigests() {
        if (mDigests == null) {
            throw new IllegalStateException("Digests not specified");
        }
        return mDigests;
    }

    /**
     * Returns {@code true} if digest restrictions have been specified.
     *
     * @see #getDigests()
     *
     * @hide
     */
    public boolean isDigestsSpecified() {
        return mDigests != null;
    }

    /**
     * Gets the set of block modes to which the key is restricted.
     *
     * @hide
     */
    public @KeyStoreKeyConstraints.BlockModeEnum int getBlockModes() {
        return mBlockModes;
    }

    /**
     * Gets the set of user authenticators which protect access to this key. The key can only be
     * used iff the user has authenticated to at least one of these user authenticators.
     *
     * @return user authenticators or {@code 0} if the key can be used without user authentication.
     *
     * @hide
     */
    public @KeyStoreKeyConstraints.UserAuthenticatorEnum int getUserAuthenticators() {
        return mUserAuthenticators;
    }

    /**
     * Gets the duration of time (seconds) for which this key can be used after the user
     * successfully authenticates to one of the associated user authenticators.
     *
     * @return duration in seconds or {@code -1} if not restricted. {@code 0} means authentication
     *         is required for every use of the key.
     *
     * @hide
     */
    public int getUserAuthenticationValidityDurationSeconds() {
        return mUserAuthenticationValidityDurationSeconds;
    }

    /**
     * Builder class for {@link KeyStoreParameter} objects.
     * <p>
     * This will build protection parameters for use with the
     * <a href="{@docRoot}training/articles/keystore.html">Android KeyStore
     * facility</a>.
     * <p>
     * This can be used to require that KeyStore entries be stored encrypted.
     * <p>
     * Example:
     *
     * <pre class="prettyprint">
     * KeyStoreParameter params = new KeyStoreParameter.Builder(mContext)
     *         .setEncryptionRequired()
     *         .build();
     * </pre>
     */
    public final static class Builder {
        private int mFlags;
        private Date mKeyValidityStart;
        private Date mKeyValidityForOriginationEnd;
        private Date mKeyValidityForConsumptionEnd;
        private @KeyStoreKeyConstraints.PurposeEnum int mPurposes;
        private @KeyStoreKeyConstraints.PaddingEnum int mPaddings;
        private @KeyStoreKeyConstraints.DigestEnum Integer mDigests;
        private @KeyStoreKeyConstraints.BlockModeEnum int mBlockModes;
        private @KeyStoreKeyConstraints.UserAuthenticatorEnum int mUserAuthenticators;
        private int mUserAuthenticationValidityDurationSeconds = -1;

        /**
         * Creates a new instance of the {@code Builder} with the given
         * {@code context}. The {@code context} passed in may be used to pop up
         * some UI to ask the user to unlock or initialize the Android KeyStore
         * facility.
         */
        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("context == null");
            }

            // Context is currently not used, but will be in the future.
        }

        /**
         * Indicates that this key must be encrypted at rest on storage. Note
         * that enabling this will require that the user enable a strong lock
         * screen (e.g., PIN, password) before creating or using the generated
         * key is successful.
         */
        public Builder setEncryptionRequired(boolean required) {
            if (required) {
                mFlags |= KeyStore.FLAG_ENCRYPTED;
            } else {
                mFlags &= ~KeyStore.FLAG_ENCRYPTED;
            }
            return this;
        }

        /**
         * Sets the time instant before which the key is not yet valid.
         *
         * <b>By default, the key is valid at any instant.
         *
         * @see #setKeyValidityEnd(Date)
         *
         * @hide
         */
        public Builder setKeyValidityStart(Date startDate) {
            mKeyValidityStart = startDate;
            return this;
        }

        /**
         * Sets the time instant after which the key is no longer valid.
         *
         * <b>By default, the key is valid at any instant.
         *
         * @see #setKeyValidityStart(Date)
         * @see #setKeyValidityForConsumptionEnd(Date)
         * @see #setKeyValidityForOriginationEnd(Date)
         *
         * @hide
         */
        public Builder setKeyValidityEnd(Date endDate) {
            setKeyValidityForOriginationEnd(endDate);
            setKeyValidityForConsumptionEnd(endDate);
            return this;
        }

        /**
         * Sets the time instant after which the key is no longer valid for encryption and signing.
         *
         * <b>By default, the key is valid at any instant.
         *
         * @see #setKeyValidityForConsumptionEnd(Date)
         *
         * @hide
         */
        public Builder setKeyValidityForOriginationEnd(Date endDate) {
            mKeyValidityForOriginationEnd = endDate;
            return this;
        }

        /**
         * Sets the time instant after which the key is no longer valid for decryption and
         * verification.
         *
         * <b>By default, the key is valid at any instant.
         *
         * @see #setKeyValidityForOriginationEnd(Date)
         *
         * @hide
         */
        public Builder setKeyValidityForConsumptionEnd(Date endDate) {
            mKeyValidityForConsumptionEnd = endDate;
            return this;
        }

        /**
         * Restricts the key to being used only for the provided set of purposes.
         *
         * <p>This restriction must be specified. There is no default.
         *
         * @hide
         */
        public Builder setPurposes(@KeyStoreKeyConstraints.PurposeEnum int purposes) {
            mPurposes = purposes;
            return this;
        }

        /**
         * Restricts the key to being used only with the provided padding schemes. Attempts to use
         * the key with any other padding will be rejected.
         *
         * <p>This restriction must be specified for keys which are used for encryption/decryption.
         *
         * @hide
         */
        public Builder setPaddings(@KeyStoreKeyConstraints.PaddingEnum int paddings) {
            mPaddings = paddings;
            return this;
        }

        /**
         * Restricts the key to being used only with the provided digests when generating signatures
         * or HMACs. Attempts to use the key with any other digest will be rejected.
         *
         * <p>For HMAC keys, the default is to restrict to the digest specified in
         * {@link Key#getAlgorithm()}. For asymmetric signing keys this constraint must be specified
         * because there is no default.
         *
         * @hide
         */
        public Builder setDigests(@KeyStoreKeyConstraints.DigestEnum int digests) {
            mDigests = digests;
            return this;
        }

        /**
         * Restricts the key to being used only with the provided block modes. Attempts to use the
         * key with any other block modes will be rejected.
         *
         * <p>This restriction must be specified for symmetric encryption/decryption keys.
         *
         * @hide
         */
        public Builder setBlockModes(@KeyStoreKeyConstraints.BlockModeEnum int blockModes) {
            mBlockModes = blockModes;
            return this;
        }

        /**
         * Sets the user authenticators which protect access to this key. The key can only be used
         * iff the user has authenticated to at least one of these user authenticators.
         *
         * <p>By default, the key can be used without user authentication.
         *
         * @param userAuthenticators user authenticators or {@code 0} if this key can be accessed
         *        without user authentication.
         *
         * @see #setUserAuthenticationValidityDurationSeconds(int)
         *
         * @hide
         */
        public Builder setUserAuthenticators(
                @KeyStoreKeyConstraints.UserAuthenticatorEnum int userAuthenticators) {
            mUserAuthenticators = userAuthenticators;
            return this;
        }

        /**
         * Sets the duration of time (seconds) for which this key can be used after the user
         * successfully authenticates to one of the associated user authenticators.
         *
         * <p>By default, the user needs to authenticate for every use of the key.
         *
         * @param seconds duration in seconds or {@code 0} if the user needs to authenticate for
         *        every use of the key.
         *
         * @see #setUserAuthenticators(int)
         *
         * @hide
         */
        public Builder setUserAuthenticationValidityDurationSeconds(int seconds) {
            mUserAuthenticationValidityDurationSeconds = seconds;
            return this;
        }

        /**
         * Builds the instance of the {@code KeyStoreParameter}.
         *
         * @throws IllegalArgumentException if a required field is missing
         * @return built instance of {@code KeyStoreParameter}
         */
        public KeyStoreParameter build() {
            return new KeyStoreParameter(mFlags,
                    mKeyValidityStart,
                    mKeyValidityForOriginationEnd,
                    mKeyValidityForConsumptionEnd,
                    mPurposes,
                    mPaddings,
                    mDigests,
                    mBlockModes,
                    mUserAuthenticators,
                    mUserAuthenticationValidityDurationSeconds);
        }
    }
}
