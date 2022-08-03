import { render, screen } from '@testing-library/react';
import App from './App';

test('renders application title', () => {
  render(<App />);
  const linkElement = screen.getByText(/document store/i);
  expect(linkElement).toBeInTheDocument();
});
