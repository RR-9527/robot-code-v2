import React from 'react';
import Head from '@docusaurus/Head';
import { GradientBackgroundWithLineDesign } from "@site/src/components/CoolLineBackground";

import styles from "./styles.module.css";
import ExpandingLogo from "@site/src/components/ExpandingLogo";
import Layout from '@theme/Layout';

export default function Home(): JSX.Element {
  const handleClick = () => {
    window.location.href = '/overview';
  }

  return (
    <Layout noFooter={true}>
      <SiteMetadata/>
      <GradientBackgroundWithLineDesign>
        <div className={styles.container}>
          <ExpandingLogo className={styles.logoWrapperOuter}/>
          <button className={styles.button} onClick={handleClick}>GO TO DOCS</button>
        </div>
      </GradientBackgroundWithLineDesign>
    </Layout>
  );
}

function SiteMetadata() {
  return (
    <Head>
      <title>Blacksmith</title>
      <meta content='Blacksmith' property='og:title'/>
      <meta content='An intuitive framework for rapid FTC software development' property='og:description'/>
      <meta content='https://blacksmithftc.vercel.app/' property='og:url'/>
      <meta content='/img/logo/blacksmith-logo-square.png' property='og:image'/>
      <meta content='#c1eab6' data-react-helmet='true' name='theme-color'/>
      <meta content='summary_large_image' name='twitter:card'/>
      <meta content="3NpXFPjNctCz2ea2e4Y__LY7BOJ85uvuawo0X_QhznI" name="google-site-verification" />
    </Head>
  )
}
