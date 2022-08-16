const Meta = ({ description, title }) => {
  return (
    <>
      <title>{title} - NHS Document Store Prototype</title>
      <meta name="description" content={description} />
    </>
  );
};

export default Meta;
