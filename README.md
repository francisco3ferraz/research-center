# Research Center Project

## Backend Setup (Wildfly + PostgreSQL)

### 1. Initial Configuration

Copy the `.env.example` file to a `.env` file:

```bash
$ cp .env.example .env
```

Make any necessary adjustments to the `.env` file.

### 2. Start Containers

Start the Wildfly and PostgreSQL containers:

```bash
$ make up
```

### 3. Build & Deploy (Maven Dependencies)

To build the project (which installs Maven dependencies) and deploy the WAR file to the running server:

```bash
$ make deploy
```

_Note: This executes `mvn clean package` and copies the artifact to the container._

### Managing Containers

**Stop and remove only local (custom) images:**

```bash
$ make down
```

**Stop and remove EVERYTHING (containers, images, volumes, networks):**

```bash
$ make down-all
```

For more command tips, check the [Docker Compose documentation](https://docs.docker.com/compose/reference/).

---

## Frontend Setup (Nuxt.js)

The frontend application is located in the `research-center-client` folder.

### 1. Install Dependencies

Navigate to the client directory and install the Node dependencies:

```bash
cd research-center-client
npm install
```

### 2. Run Development Server

Start the development server:

```bash
npm run dev
```

The application will be available at http://localhost:3000.
