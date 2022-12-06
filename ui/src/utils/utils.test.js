import {downloadFile, formatSize, setUrlHostToLocalHost} from './utils';

describe('utils', () => {
    describe('setUrlHostToLocalHost utility', () => {
        let env = '';
        beforeAll(() => {
            env = process.env.NODE_ENV;
        });
        afterAll(() => {
            process.env.NODE_ENV = env;
        });
        test('change url host to local host if in the development environment', () => {
            process.env.NODE_ENV = 'development';
            const url = 'http://host:1234/test';
            const updatedUrl = setUrlHostToLocalHost(url);
            expect(updatedUrl).toBe('http://localhost:1234/test');
        });
        test('not change url host to local host if not in the development environment', () => {
            process.env.NODE_ENV = 'production';
            const url = 'http://host:1234/test';
            const updatedUrl = setUrlHostToLocalHost(url);
            expect(updatedUrl).toBe('http://host:1234/test');
        });
    });

    describe('Formatting file size', () => {
        test('should convert the size into appropriate storage unit upto GB',
            () => {
                const testCases = [
                    {
                        bytes: 1023456,
                        expected: '999 KB'
                    },
                    {
                        bytes: 1023456000,
                        expected: '976 MB'
                    },
                    {
                        bytes: 1000000000000,
                        expected: '931 GB'
                    },
                    {
                        bytes: 0,
                        expected: '0 bytes'
                    }
                ]

                testCases.forEach(testCase => {
                    expect(formatSize(testCase.bytes)).toEqual(testCase.expected)
                })
            });

        test('it should throw an error if the size is less than zero', () => {
            expect(() => formatSize(-1)).toThrow()
        })

        test('it should throw an error if file size is greater than a terabyte', () => {
            expect(() => formatSize(10000000000000)).toThrow()
        })
    });

    describe('downloadFile', () => {
        it('creates and clicks a link once', () => {
            const link = {click: jest.fn()};
            jest.spyOn(document, 'createElement').mockImplementation(() => link);

            downloadFile('some-url', 'some-filename');

            expect(link.click).toHaveBeenCalledTimes(1);
        });

        it('creates a link with a given URL', () => {
            const link = {click: jest.fn()};
            const url = 'some-url';
            jest.spyOn(document, 'createElement').mockImplementation(() => link);

            downloadFile(url, 'some-filename');

            expect(link.href).toEqual(url);
        });

        it('creates a link with the to be downloaded filename', () => {
            const link = {click: jest.fn()};
            const filename = 'some-filename';
            jest.spyOn(document, 'createElement').mockImplementation(() => link);

            downloadFile('some-url', filename);

            expect(link.download).toEqual(filename);
        });
    });
});
