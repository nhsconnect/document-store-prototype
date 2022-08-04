import { render, screen, within } from '@testing-library/react';
import App from './App';

test('renders application title', () => {
  render(<App />);
  const linkElement = within(screen.getByTestId("header-service-name")).getByText("Document Store");
  expect(linkElement).toBeTruthy();
});
