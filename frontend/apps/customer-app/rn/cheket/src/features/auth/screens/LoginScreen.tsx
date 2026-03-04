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
import type {AuthStackParamList} from '../../../core/navigation/types';

type LoginNavProp = NativeStackNavigationProp<AuthStackParamList, 'Login'>;

export function LoginScreen(): React.JSX.Element {
  const navigation = useNavigation<LoginNavProp>();
  const login = useAuthStore(state => state.login);

  const [id, setId] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleLogin = () => {
    setErrorMessage('');

    if (!id.trim()) {
      setErrorMessage('아이디를 입력해주세요.');
      return;
    }
    if (!password.trim()) {
      setErrorMessage('비밀번호를 입력해주세요.');
      return;
    }

    const success = login(id, password);
    if (!success) {
      setErrorMessage('아이디 또는 비밀번호가 올바르지 않습니다.');
    }
    // On success, RootNavigator automatically switches to MainTabs
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled">
          {/* Logo */}
          <View style={styles.logoContainer}>
            <Text style={styles.logoText}>Cheket</Text>
          </View>

          {/* Title */}
          <Text style={styles.title}>로그인</Text>

          {/* ID Input */}
          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>아이디</Text>
            <View style={styles.inputWrapper}>
              <TextInput
                style={styles.textInput}
                placeholder="아이디를 입력하세요"
                placeholderTextColor={colors.mutedForeground}
                value={id}
                onChangeText={text => {
                  setId(text);
                  if (errorMessage) {
                    setErrorMessage('');
                  }
                }}
                autoCapitalize="none"
                autoCorrect={false}
              />
            </View>
          </View>

          {/* Password Input */}
          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>비밀번호</Text>
            <View style={styles.inputWrapper}>
              <TextInput
                style={[styles.textInput, styles.passwordInput]}
                placeholder="비밀번호를 입력하세요"
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
                autoCorrect={false}
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

          {/* Error Message */}
          {errorMessage !== '' && (
            <Text style={styles.errorText}>{errorMessage}</Text>
          )}

          {/* Login Button */}
          <PrimaryButton
            title="로그인"
            onPress={handleLogin}
            style={styles.loginButton}
          />

          {/* Sign Up Link */}
          <View style={styles.signUpContainer}>
            <Text style={styles.signUpText}>아직 회원이 아니신가요? </Text>
            <Pressable onPress={() => navigation.navigate('SignUp')}>
              <Text style={styles.signUpLink}>회원가입</Text>
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
    paddingTop: 60,
    paddingBottom: spacing.xl,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: spacing.xl,
  },
  logoText: {
    fontSize: 28,
    fontWeight: '700',
    color: colors.primary,
  },
  title: {
    ...typography.h1,
    color: colors.foreground,
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
  errorText: {
    ...typography.bodySm,
    color: colors.danger,
    marginBottom: spacing.md,
  },
  loginButton: {
    marginTop: spacing.sm,
    marginBottom: spacing.lg,
  },
  signUpContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  signUpText: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  signUpLink: {
    ...typography.label,
    color: colors.primary,
  },
});
