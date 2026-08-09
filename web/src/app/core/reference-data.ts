// Single source of truth for the organisation's fixed reference data. These are
// a known, closed set for this single org (mirroring the backend seed script),
// so both the directory filters and the create/edit form use these static lists
// rather than a facets endpoint - one less request, one less moving part. Using
// the same lists everywhere also guarantees a created employee's department/
// country/level always match the values the directory filters offer.
export const DEPARTMENTS = [
  'Customer Support', 'Design', 'Engineering', 'Finance', 'Human Resources',
  'Legal', 'Marketing', 'Operations', 'Product', 'Sales',
];

export const COUNTRIES = [
  'Australia', 'Brazil', 'Canada', 'France', 'Germany', 'India', 'Japan',
  'Singapore', 'United Kingdom', 'United States',
];

export const LEVELS = ['L1', 'L2', 'L3', 'L4', 'L5', 'L6', 'L7'];

export const CURRENCIES = ['USD', 'GBP', 'EUR', 'INR', 'CAD', 'AUD', 'SGD', 'JPY', 'BRL'];
