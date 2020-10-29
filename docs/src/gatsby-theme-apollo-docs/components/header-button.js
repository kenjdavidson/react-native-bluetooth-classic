import React from "react";
import styled from "@emotion/styled";
import { IconGithub } from "@apollo/space-kit/icons/IconGithub";
import { breakpoints } from "gatsby-theme-apollo-core";

const Container = styled.div({
  display: "flex",
  flexShrink: 0,
  width: 240,
  [breakpoints.lg]: {
    width: "auto",
    marginRight: 0,
  },
  [breakpoints.md]: {
    display: "none",
  },
});

const StyledLink = styled.a({
  display: "flex",
  alignItems: "center",
  lineHeight: 2,
  textDecoration: "none",
  color: "#008ecf",
});

const StyledIcon = styled(IconGithub)({
  height: "0.75em",
  marginLeft: "0.5em",
  fontSize: "1.5em"
});

export default function HeaderButton() {
  return (
    <Container>
      <StyledLink
        href="https://github.com/kenjdavidson/react-native-bluetooth-classic/issues"
        target="_blank"
        rel="noopener noreferrer"
      >
        Report a Bug
        <StyledIcon weight="thin" />
      </StyledLink>
    </Container>
  );
}
