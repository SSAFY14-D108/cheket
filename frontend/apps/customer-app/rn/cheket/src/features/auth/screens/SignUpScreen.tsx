import React, {useState} from 'react';
import {
  View,
  Text,
  TextInput,
  StyleSheet,
  SafeAreaView,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import {spacing} from '../../../core/theme/spacing';
import {useAuthStore} from '../../../store/authStore';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import type {AuthStackParamList} from '../../../core/navigation/types';

type SignUpNavProp = NativeStackNavigationProp<AuthStackParamList, 'SignUp'>;

export function SignUpScreen(): React.JSX.Element {
  const navigation = useNavigation<SignUpNavProp>();
  const login = useAuthStore(state => state.login);

  // Step tracking
  const [step, setStep] = useState<1 | 2>(1);

  // Step 1 fields
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [verificationSent, setVerificationSent] = useState(false);
  const [verificationCode, setVerificationCode] = useState('');
  const [isVerified, setIsVerified] = useState(false);

  // Step 2 fields
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);
  const [agreeTerms, setAgreeTerms] = useState(false);
  const [agreePrivacy, setAgreePrivacy] = useState(false);
  const [agreeMarketing, setAgreeMarketing] = useState(false);

  // Error
  const [errorMessage, setErrorMessage] = useState('');

  // Phone number formatting: 010-XXXX-XXXX
  const formatPhoneNumber = (text: string): string => {
    const digits = text.replace(/\D/g, '');
    const limited = digits.slice(0, 11);

    if (limited.length <= 3) {
      return limited;
    }
    if (limited.length <= 7) {
      return `${limited.slice(0, 3)}-${limited.slice(3)}`;
    }
    return `${limited.slice(0, 3)}-${limited.slice(3, 7)}-${limited.slice(7)}`;
  };

  const handlePhoneChange = (text: string) => {
    setPhone(formatPhoneNumber(text));
  };

  const handleSendVerification = () => {
    setErrorMessage('');
    const digits = phone.replace(/\D/g, '');
    if (digits.length < 10) {
      setErrorMessage('올바른 전화번호를 입력해주세요.');
      return;
    }
    setVerificationSent(true);
  };

  const handleVerifyCode = () => {
    setErrorMessage('');
    if (verificationCode.length !== 6) {
      setErrorMessage('인증번호 6자리를 입력해주세요.');
      return;
    }
    // Mock: any 6-digit code works
    setIsVerified(true);
  };

  const handleNextStep = () => {
    setErrorMessage('');
    if (!name.trim()) {
      setErrorMessage('이름을 입력해주세요.');
      return;
    }
    if (!isVerified) {
      setErrorMessage('전화번호 인증을 완료해주세요.');
      return;
    }
    setStep(2);
  };

  const handleSignUp = () => {
    setErrorMessage('');

    if (password.length < 6) {
      setErrorMessage('비밀번호는 6자 이상이어야 합니다.');
      return;
    }
    if (password !== passwordConfirm) {
      setErrorMessage('비밀번호가 일치하지 않습니다.');
      return;
    }
    if (!agreeTerms) {
      setErrorMessage('서비스 이용약관에 동의해주세요.');
      return;
    }
    if (!agreePrivacy) {
      setErrorMessage('개인정보 처리방침에 동의해주세요.');
      return;
    }

    const success = login(name, password);
    if (success) {
      // RootNavigator will automatically switch to MainTabs
    } else {
      setErrorMessage('회원가입에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const renderStep1 = () => (
    <>
      {/* Name */}
      <View style={styles.inputContainer}>
        <Text style={styles.inputLabel}>이름</Text>
        <View style={styles.inputWrapper}>
          <TextInput
            style={styles.textInput}
            placeholder="이름을 입력하세요"
            placeholderTextColor={colors.mutedForeground}
            value={name}
            onChangeText={text => {
              setName(text);
              if (errorMessage) {
                setErrorMessage('');
              }
            }}
            autoCapitalize="words"
          />
        </View>
      </View>

      {/* Phone Number */}
      <View style={styles.inputContainer}>
        <Text style={styles.inputLabel}>전화번호</Text>
        <View style={styles.phoneRow}>
          <View style={[styles.inputWrapper, styles.phoneInput]}>
            <TextInput
              style={styles.textInput}
              placeholder="010-0000-0000"
              placeholderTextColor={colors.mutedForeground}
              value={phone}
              onChangeText={text => {
                handlePhoneChange(text);
                if (errorMessage) {
                  setErrorMessage('');
                }
              }}
              keyboardType="phone-pad"
              maxLength={13}
            />
          </View>
          <SecondaryButton
            title="인증번호 전송"
            onPress={handleSendVerification}
            disabled={verificationSent && isVerified}
            style={styles.sendButton}
          />
        </View>
      </View>

      {/* Verification Code */}
      {verificationSent && (
        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>인증번호</Text>
          <View style={styles.phoneRow}>
            <View style={[styles.inputWrapper, styles.phoneInput]}>
              <TextInput
                style={styles.textInput}
                placeholder="인증번호 6자리"
                placeholderTextColor={colors.mutedForeground}
                value={verificationCode}
                onChangeText={text => {
                  setVerificationCode(text.replace(/\D/g, '').slice(0, 6));
                  if (errorMessage) {
                    setErrorMessage('');
                  }
                }}
                keyboardType="number-pad"
                maxLength={6}
                editable={!isVerified}
              />
            </View>
            <SecondaryButton
              title={isVerified ? '인증완료' : '확인'}
              onPress={handleVerifyCode}
              disabled={isVerified}
              style={styles.sendButton}
            />
          </View>
          {isVerified && (
            <Text style={styles.verifiedText}>인증이 완료되었습니다.</Text>
          )}
        </View>
      )}

      {/* Error */}
      {errorMessage !== '' && (
        <Text style={styles.errorText}>{errorMessage}</Text>
      )}

      {/* Next Button */}
      <PrimaryButton
        title="다음"
        onPress={handleNextStep}
        disabled={!name.trim() || !isVerified}
        style={styles.actionButton}
      />
    </>
  );

  const renderStep2 = () => (
    <>
      {/* Password */}
      <View style={styles.inputContainer}>
        <Text style={styles.inputLabel}>비밀번호</Text>
        <View style={styles.inputWrapper}>
          <TextInput
            style={[styles.textInput, styles.passwordInput]}
            placeholder="비밀번호 (6자 이상)"
            placeholderTextColor={colors.mutedForeground}
            value={password}
            onChangeText={text => {
              setPassword(text);
              if (errorMessage) {
                setErrorMessage('');
              }
            }}
            secureTextEntry={!showPassword}
            autoCapitalize="none"
          />
          <Pressable
            style={styles.eyeButton}
            onPress={() => setShowPassword(prev => !prev)}
            hitSlop={8}>
            <Text style={styles.eyeText}>
              {showPassword ? '숨기기' : '보기'}
            </Text>
          </Pressable>
        </View>
      </View>

      {/* Password Confirm */}
      <View style={styles.inputContainer}>
        <Text style={styles.inputLabel}>비밀번호 확인</Text>
        <View style={styles.inputWrapper}>
          <TextInput
            style={[styles.textInput, styles.passwordInput]}
            placeholder="비밀번호를 다시 입력하세요"
            placeholderTextColor={colors.mutedForeground}
            value={passwordConfirm}
            onChangeText={text => {
              setPasswordConfirm(text);
              if (errorMessage) {
                setErrorMessage('');
              }
            }}
            secureTextEntry={!showPasswordConfirm}
            autoCapitalize="none"
          />
          <Pressable
            style={styles.eyeButton}
            onPress={() => setShowPasswordConfirm(prev => !prev)}
            hitSlop={8}>
            <Text style={styles.eyeText}>
              {showPasswordConfirm ? '숨기기' : '보기'}
            </Text>
          </Pressable>
        </View>
      </View>

      {/* Terms Checkboxes */}
      <View style={styles.termsContainer}>
        <Text style={styles.termsTitle}>약관 동의</Text>

        <Pressable
          style={styles.checkboxRow}
          onPress={() => setAgreeTerms(prev => !prev)}>
          <View
            style={[styles.checkbox, agreeTerms && styles.checkboxChecked]}>
            {agreeTerms && <Text style={styles.checkmark}>{'✓'}</Text>}
          </View>
          <Text style={styles.checkboxLabel}>
            <Text style={styles.requiredTag}>[필수] </Text>
            서비스 이용약관 동의
          </Text>
        </Pressable>

        <Pressable
          style={styles.checkboxRow}
          onPress={() => setAgreePrivacy(prev => !prev)}>
          <View
            style={[styles.checkbox, agreePrivacy && styles.checkboxChecked]}>
            {agreePrivacy && <Text style={styles.checkmark}>{'✓'}</Text>}
          </View>
          <Text style={styles.checkboxLabel}>
            <Text style={styles.requiredTag}>[필수] </Text>
            개인정보 처리방침 동의
          </Text>
        </Pressable>

        <Pressable
          style={styles.checkboxRow}
          onPress={() => setAgreeMarketing(prev => !prev)}>
          <View
            style={[
              styles.checkbox,
              agreeMarketing && styles.checkboxChecked,
            ]}>
            {agreeMarketing && <Text style={styles.checkmark}>{'✓'}</Text>}
          </View>
          <Text style={styles.checkboxLabel}>
            <Text style={styles.optionalTag}>[선택] </Text>
            마케팅 정보 수신 동의
          </Text>
        </Pressable>
      </View>

      {/* Error */}
      {errorMessage !== '' && (
        <Text style={styles.errorText}>{errorMessage}</Text>
      )}

      {/* Sign Up Button */}
      <PrimaryButton
        title="가입 완료"
        onPress={handleSignUp}
        disabled={
          password.length < 6 ||
          password !== passwordConfirm ||
          !agreeTerms ||
          !agreePrivacy
        }
        style={styles.actionButton}
      />
    </>
  );

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled">
          {/* Header with back button for step 2 */}
          <View style={styles.headerRow}>
            {step === 2 && (
              <Pressable
                onPress={() => {
                  setStep(1);
                  setErrorMessage('');
                }}
                hitSlop={8}>
                <Text style={styles.backText}>{'< 이전'}</Text>
              </Pressable>
            )}
          </View>

          {/* Title */}
          <Text style={styles.title}>회원가입</Text>

          {/* Step indicator */}
          <View style={styles.stepIndicator}>
            <View
              style={[
                styles.stepDot,
                step === 1 && styles.stepDotActive,
              ]}
            />
            <View style={styles.stepLine} />
            <View
              style={[
                styles.stepDot,
                step === 2 && styles.stepDotActive,
              ]}
            />
          </View>
          <Text style={styles.stepLabel}>
            {step === 1 ? '본인 인증' : '비밀번호 설정'}
          </Text>

          {/* Form */}
          {step === 1 ? renderStep1() : renderStep2()}

          {/* Login link */}
          <View style={styles.loginContainer}>
            <Text style={styles.loginText}>이미 계정이 있으신가요? </Text>
            <Pressable onPress={() => navigation.navigate('Login')}>
              <Text style={styles.loginLink}>로그인</Text>
            </Pressable>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.background,
  },
  flex: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.xl,
    paddingBottom: spacing.xl,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: 32,
    marginBottom: spacing.sm,
  },
  backText: {
    ...typography.label,
    color: colors.primary,
  },
  title: {
    ...typography.h1,
    color: colors.foreground,
    marginBottom: spacing.md,
  },
  stepIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.sm,
  },
  stepDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: colors.border,
  },
  stepDotActive: {
    backgroundColor: colors.primary,
  },
  stepLine: {
    width: 40,
    height: 2,
    backgroundColor: colors.border,
    marginHorizontal: spacing.sm,
  },
  stepLabel: {
    ...typography.bodySm,
    color: colors.mutedForeground,
    textAlign: 'center',
    marginBottom: spacing.lg,
  },
  inputContainer: {
    marginBottom: spacing.md,
  },
  inputLabel: {
    ...typography.label,
    color: colors.foreground,
    marginBottom: spacing.sm,
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.input,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    paddingHorizontal: spacing.md,
    height: 48,
  },
  textInput: {
    flex: 1,
    ...typography.body,
    color: colors.foreground,
    padding: 0,
    height: '100%',
  },
  passwordInput: {
    paddingRight: spacing.xs,
  },
  eyeButton: {
    paddingLeft: spacing.sm,
    justifyContent: 'center',
    height: '100%',
  },
  eyeText: {
    ...typography.labelSm,
    color: colors.mutedForeground,
  },
  phoneRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  phoneInput: {
    flex: 1,
  },
  sendButton: {
    width: 120,
    height: 48,
  },
  verifiedText: {
    ...typography.bodySm,
    color: colors.success,
    marginTop: spacing.xs,
  },
  termsContainer: {
    marginTop: spacing.sm,
    marginBottom: spacing.md,
    padding: spacing.md,
    backgroundColor: colors.card,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
  },
  termsTitle: {
    ...typography.label,
    color: colors.foreground,
    marginBottom: spacing.md,
  },
  checkboxRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  checkbox: {
    width: 22,
    height: 22,
    borderRadius: 4,
    borderWidth: 2,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.sm,
  },
  checkboxChecked: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  checkmark: {
    color: colors.white,
    fontSize: 14,
    fontWeight: '700',
  },
  checkboxLabel: {
    ...typography.body,
    color: colors.foreground,
    flex: 1,
  },
  requiredTag: {
    ...typography.label,
    color: colors.primary,
  },
  optionalTag: {
    ...typography.label,
    color: colors.mutedForeground,
  },
  errorText: {
    ...typography.bodySm,
    color: colors.danger,
    marginBottom: spacing.md,
  },
  actionButton: {
    marginTop: spacing.sm,
    marginBottom: spacing.lg,
  },
  loginContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  loginText: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  loginLink: {
    ...typography.label,
    color: colors.primary,
  },
});
