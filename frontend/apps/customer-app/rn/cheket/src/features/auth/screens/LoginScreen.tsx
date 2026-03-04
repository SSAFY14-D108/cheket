import React, {useState} from 'react';
import {Button, SafeAreaView, Text} from 'react-native';
import {AuthHeader} from '../components/AuthHeader';
import {AuthTextField} from '../components/AuthTextField';
import {useLogin} from '../hooks/useLogin';

export function LoginScreen(): React.JSX.Element {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const loginMutation = useLogin();

  return (
    <SafeAreaView style={{padding: 16}}>
      <AuthHeader title="Login" />
      <AuthTextField value={email} onChangeText={setEmail} placeholder="Email" />
      <AuthTextField value={password} onChangeText={setPassword} placeholder="Password" />
      <Button
        title={loginMutation.isPending ? 'Loading...' : 'Login'}
        disabled={loginMutation.isPending}
        onPress={() => loginMutation.mutate({email, password})}
      />
      {loginMutation.isError && <Text style={{color: '#EF4444'}}>Login failed</Text>}
    </SafeAreaView>
  );
}
