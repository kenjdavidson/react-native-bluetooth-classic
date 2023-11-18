import React from 'react';
import { withPrefix } from "gatsby"

export default function Logo(props) {
  const logoPath = withPrefix('/bc-logo.svg');

  return (
    <div>
      <img src={ logoPath } width='230px' alt='React Native Logo' />     
    </div>
  );
}