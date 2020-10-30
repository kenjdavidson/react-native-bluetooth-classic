import Typography from 'typography';
import kirkhamTheme from 'typography-theme-kirkham';

let theme = {
  ...kirkhamTheme,
  googleFonts: [
    {
      name: 'Playfair Display',
      styles: ['400', '500', '700', '900']
    },
    {
      name: 'Merriweather',
      styles: ['300', '300i', '400', '400i', '700',]
    },
    {
      name: 'Mrs Saint Delafield',
      styles: ['400']
    }
  ],
  scriptFontFamily: ['Mrs Saint Delafield', 'cursive'],
  headerFontFamily: ['Playfair Display', 'serif'],
  headerWeight: 700,  
  headerColor: 'var(--base05)',
  bodyFontFamily: ['Merriweather', 'serif'],
  bodyWeight: 300,
  bodyColor: 'var(--base05)',
  boldWeight: 800,
  baseFontSize: '14px',
};

theme.overrideThemeStyles = ({ rhythm }, options) => ({
  'body': {
    fontFamily: options.bodyFontFamily.join(',') + ' !important'
  },
});

const typography = new Typography(theme);
export const { scale, rhythm, options } = typography;
export default typography;