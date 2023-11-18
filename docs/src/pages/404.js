import React from 'react';
import { Link } from 'gatsby';

import Layout from '@rocketseat/gatsby-theme-docs/src/components/Layout';
import Seo from '@rocketseat/gatsby-theme-docs/src/components/SEO';

export default function NotFound() {
  return (
    <Layout title="This is not the page you're looking for...">
      <Seo title="404: Not found" />
      <p>
        Looks like you're trying to find something that I haven't had a chance to get working, documented or even thought of implementing.
      </p>
      <p>
        Feel free to open a pull request if you feel like someone else may benefit from the documentation you're expecting.  Alternatively open
        an issue on Github and it'll be gotten to when possible.
      </p>
      <p>
        Get back to some common pages:
        <ul>
          <li><Link to="/">Getting Started</Link></li>
          <li><Link to="/guides/using-with-expo">Using with Expo</Link></li>
          <li><Link to="/guides/hex-encoding-decoding">Adding custom Hex/Binary connection</Link></li>
        </ul>
      </p>
      <p>
        As always, thanks for using this library!
      </p>
    </Layout>
  );
}
