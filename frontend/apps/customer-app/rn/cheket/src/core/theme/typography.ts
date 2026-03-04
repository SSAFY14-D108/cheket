import {TextStyle} from 'react-native';

export const typography = {
  h1: {fontSize: 24, fontWeight: '700', lineHeight: 32} as TextStyle,
  h2: {fontSize: 20, fontWeight: '700', lineHeight: 28} as TextStyle,
  h3: {fontSize: 16, fontWeight: '600', lineHeight: 24} as TextStyle,

  bodyLg: {fontSize: 16, fontWeight: '400', lineHeight: 24} as TextStyle,
  body: {fontSize: 14, fontWeight: '400', lineHeight: 20} as TextStyle,
  bodySm: {fontSize: 12, fontWeight: '400', lineHeight: 16} as TextStyle,

  label: {fontSize: 14, fontWeight: '600'} as TextStyle,
  labelSm: {fontSize: 12, fontWeight: '600'} as TextStyle,
  caption: {fontSize: 10, fontWeight: '500'} as TextStyle,

  price: {fontSize: 16, fontWeight: '700'} as TextStyle,
  priceLg: {fontSize: 24, fontWeight: '700'} as TextStyle,
} as const;
