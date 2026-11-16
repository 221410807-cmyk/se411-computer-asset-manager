import team from '../data/team.js';

// About page: who built the app and what it does.
export default function AboutPage() {
  return (
    <section>
      <div className="page-header">
        <h1>About</h1>
        <p className="mode-badge">SE411 Software Construction · Fall 2026</p>
      </div>

      <div className="about-grid">
        <div className="card">
          <h2>Our team</h2>
          <table>
            <caption className="visually-hidden">Team members</caption>
            <thead>
              <tr>
                <th scope="col">Name</th>
                <th scope="col">Student ID</th>
              </tr>
            </thead>
            <tbody>
              {team.map((member, index) => (
                <tr key={index}>
                  <td>{member.name}</td>
                  <td className="mono">{member.studentId}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <h2>The project</h2>
          <p>
            Computer Asset Manager helps a company keep track of its computers. Each computer has an
            ID, a name, a brand, a serial number and a status (Available, In use, In repair or
            Retired).
          </p>
          <ul>
            <li>Part 1: a React app that stores sample data in a JavaScript array.</li>
            <li>Part 2: the same app connected to a Java Spring Boot REST API.</li>
          </ul>
          <p className="muted">Built with React, React Router, Vite, Spring Boot and Maven.</p>
        </div>
      </div>
    </section>
  );
}
